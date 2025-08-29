package ftn.project.data.database;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;

public class FirestoreSync {
    private FirestoreSync() {}

    public static void mirrorUserEquipmentToFirestore(
            Context ctx, String uid, int userId, UserEquipment ue) {

        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("equipmentId", ue.getEquipmentId());
        data.put("userId", userId); // tvoj intId (opciono)
        data.put("battleCount", ue.getBattleCount() == null ? 0 : ue.getBattleCount());
        data.put("active", ue.getActive());


        fs.collection("users").document(uid)
                .collection("equipmentInstances")
                .add(data)
                .addOnSuccessListener(ref -> Log.d("FS_SYNC", "Created instance: " + ref.getId()))
                .addOnFailureListener(e ->
                        Toast.makeText(ctx, "Sync equipment fail: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
      }

    public static void mirrorUserBadgeToFirestore(
            Context ctx, String uid, int userId, String badgeCode) {

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        DocumentReference doc = fs.collection("users").document(uid)
                .collection("badges").document(badgeCode);

        Map<String, Object> data = new HashMap<>();
        data.put("badgeCode", badgeCode);
        data.put("earnedAt", FieldValue.serverTimestamp());

        doc.set(data, SetOptions.merge())
                .addOnFailureListener(e -> Toast.makeText(ctx, "Sync badge fail: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static void syncUserEquipmentDown(
            Context ctx,
            AppDatabase db,
            String firebaseUid,
            @androidx.annotation.Nullable Runnable onDone
    ) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        fs.collection("users")
                .document(firebaseUid)
                .collection("equipmentInstances")
                .get()
                .addOnSuccessListener(snaps ->
                        Executors.newSingleThreadExecutor().execute(() -> {
                            try {
                                // 1) Učitaj lokalnog korisnika
                                User me = db.userRepository().getByFirebaseUid(firebaseUid);
                                if (me == null) {
                                    postMain(onDone);
                                    return;
                                }
                                int meId = me.getUserId();

                                int inserted = 0, updated = 0, skipped = 0;

                                // 2) Prođi kroz sve instance iz Firestore-a
                                for (com.google.firebase.firestore.DocumentSnapshot d : snaps) {
                                    Long eqL = d.getLong("equipmentId");
                                    if (eqL == null) { skipped++; continue; }
                                    int equipmentId = eqL.intValue();

                                    Long bcL = d.getLong("battleCount");
                                    Integer battleCount = (bcL == null) ? 0 : bcL.intValue();

                                    Boolean activeB = d.getBoolean("active");
                                    boolean active = (activeB != null) && activeB;

                                    try {
                                        // 3) Učitaj postojeći zapis (po PK: userId+equipmentId)
                                        //    Prilagodi imenu metode tvog DAO-a ako je drugačije:
                                        //    npr. getByUserAndEquipment / get / findOne
                                        UserEquipment existing =
                                                db.userEquipmentRepository().getByUserAndEquipment(meId, equipmentId);

                                        if (existing == null) {
                                            // (Ako FK na Equipment ne postoji lokalno, insert može da padne;
                                            //  pretpostavka je da imaš seed-ovan Equipment.)
                                            UserEquipment ue = new UserEquipment();
                                            ue.setUserId(meId);
                                            ue.setEquipmentId(equipmentId);
                                            ue.setBattleCount(battleCount);
                                            ue.setActive(active);
                                            db.userEquipmentRepository().add(ue);
                                            inserted++;
                                        } else {
                                            boolean changed = false;

                                            Integer oldBc = existing.getBattleCount();
                                            if (oldBc == null || !oldBc.equals(battleCount)) {
                                                existing.setBattleCount(battleCount);
                                                changed = true;
                                            }

                                            Boolean oldActive = existing.getActive();
                                            if (oldActive == null || oldActive.booleanValue() != active) {
                                                existing.setActive(active);
                                                changed = true;
                                            }

                                            if (changed) {
                                                db.userEquipmentRepository().update(existing);
                                                updated++;
                                            }
                                        }
                                    } catch (Exception ex) {
                                        skipped++;
                                        Log.w("FS_SYNC", "UE upsert skip (eqId=" + equipmentId + "): " + ex.getMessage());
                                    }
                                }

                                Log.d("FS_SYNC", "UserEquipment down: ins=" + inserted +
                                        " upd=" + updated + " skip=" + skipped);
                            } finally {
                                postMain(onDone);
                            }
                        })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(ctx, "Sync equipment fail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    postMain(onDone);
                });
    }

    public static void syncAllUsersDown(Context ctx, AppDatabase db, Runnable onDone) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener(snaps -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        int count = 0;
                        for (var d : snaps) {
                            String uid = d.getId();
                            Long intIdL = d.getLong("intId");
                            String username = d.getString("username");
                            String avatar = d.getString("avatarImage");
                            Long levelL = d.getLong("level");

                            if (intIdL == null) continue;
                            int intId = intIdL.intValue();
                            int level  = levelL == null ? 1 : levelL.intValue();

                            User local = db.userRepository().getByFirebaseUid(uid);
                            if (local == null) {
                                User u = new User();
                                u.setUserId(intId);
                                u.setFirebaseUid(uid);
                                u.setUsername(username);
                                u.setAvatarImage(avatar);
                                u.setLevel(level);
                                db.userRepository().insert(u);
                            } else {
                                // osveži osnovna polja ako želiš
                                boolean changed = false;
                                if (username != null && !username.equals(local.getUsername())) {
                                    local.setUsername(username); changed = true;
                                }
                                if (avatar != null && (local.getAvatarImage()==null || !avatar.equals(local.getAvatarImage()))) {
                                    local.setAvatarImage(avatar); changed = true;
                                }
                                if (level > 0 && level != local.getLevel()) { local.setLevel(level); changed = true; }
                                if (changed) db.userRepository().update(local);
                            }
                            count++;
                        }
                        if (onDone != null) new android.os.Handler(Looper.getMainLooper()).post(onDone);
                        Log.d("FS_SYNC","Synced public users: "+count);
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ctx, "Sync users fail: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    // Mirror upisa prijateljstva u cloud (Accepted odmah, po tvojoj trenutnoj logici)
    public static void mirrorFriendshipToFirestore(
            Context ctx,
            String myUid,      // Firebase UID current user
            int myUserId,      // lokalni int ID current user-a
            String otherUid,   // Firebase UID prijatelja
            @androidx.annotation.Nullable Integer otherUserId // može null ako ne znaš još
    ) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        // stabilan ključ dokumenta: leksikografski sortiran par UID-ova
        String a = (myUid.compareTo(otherUid) <= 0) ? myUid : otherUid;
        String b = (a.equals(myUid)) ? otherUid : myUid;
        String pairKey = a + "_" + b;

        DocumentReference doc = fs.collection("friendships").document(pairKey);

        java.util.List<String> uids = java.util.Arrays.asList(a, b);

        java.util.Map<String, Object> uidToId = new java.util.HashMap<>();
        uidToId.put(myUid, myUserId);
        if (otherUserId != null) {
            uidToId.put(otherUid, otherUserId);
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("uids", uids);
        data.put("uidToId", uidToId); // merge-ovaćemo, pa će se popuniti i drugi ID kad ga saznaš
        data.put("status", "accepted");
        data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        doc.set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(v -> android.util.Log.d("FS_SYNC", "Friendship mirror OK: " + pairKey))
                .addOnFailureListener(e ->
                        android.widget.Toast.makeText(ctx, "Sync friendship fail: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Povuci SVA prijateljstva koja uključuju myUid i upiši u lokalni Room.
     * Pretpostavka: imaš lokalno kreiranog "me" korisnika (po myUid).
     * Za "drugog" korisnika, ako ne postoji lokalno, napravimo stub i/ili
     * oslonimo se na publicUsers sync koji već imaš.
     */
    public static void syncFriendshipsDown(
            Context ctx,
            ftn.project.data.db.AppDatabase db,
            String myUid,
            @androidx.annotation.Nullable Runnable onDone
    ) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        fs.collection("friendships")
                .whereArrayContains("uids", myUid)
                .get()
                .addOnSuccessListener(snaps -> java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        // 1) "me" iz lokalne baze
                        ftn.project.domain.entity.User me = db.userRepository().getByFirebaseUid(myUid);
                        if (me == null) {
                            // nema lokalnog korisnika – nema smisla dalje
                            postMain(onDone);
                            return;
                        }

                        int meId = me.getUserId();

                        for (com.google.firebase.firestore.DocumentSnapshot d : snaps) {
                            java.util.List<String> uids = (java.util.List<String>) d.get("uids");
                            if (uids == null || uids.size() != 2) continue;
                            String uidA = uids.get(0);
                            String uidB = uids.get(1);
                            String otherUid = myUid.equals(uidA) ? uidB : uidA;

                            // 2) "other" user: probaj lokalno; ako nema, napravi stub
                            ftn.project.domain.entity.User other = db.userRepository().getByFirebaseUid(otherUid);
                            if (other == null) {
                                other = new ftn.project.domain.entity.User();
                                other.setFirebaseUid(otherUid);
                                other.setUsername("Friend"); // biće osvežen preko publicUsers sync-a
                                long rid = db.userRepository().insert(other);
                                if (rid == -1) {
                                    // ako INSERT fail zbog unique, učitaj opet
                                    other = db.userRepository().getByFirebaseUid(otherUid);
                                } else {
                                    other.setUserId((int) rid);
                                }
                            }
                            if (other == null) continue; // safety

                            int otherId = other.getUserId();

                            // 3) Ako nema lokalno prijateljstvo – upiši
                            boolean exists = db.friendshipRepository().existsBetween(meId, otherId);
                            if (!exists) {
                                ftn.project.domain.entity.Friendship f = new ftn.project.domain.entity.Friendship();
                                f.setFirstUserId(meId);
                                f.setSecondUserId(otherId);
                                db.friendshipRepository().insert(f);
                            }

                            // 4) (opciono) ako doc sadrži uidToId, pokušaj da dopuniš nedostajući ID u cloudu
                            java.util.Map<String, Object> uidToId = (java.util.Map<String, Object>) d.get("uidToId");
                            boolean needBackfill = false;
                            if (uidToId == null || !uidToId.containsKey(myUid) || !uidToId.containsKey(otherUid)) {
                                needBackfill = true;
                            }
                            if (needBackfill) {
                                // dopuni cloud mapu da i druga strana kasnije ima int ID
                                java.util.Map<String, Object> patch = new java.util.HashMap<>();
                                java.util.Map<String, Object> map = new java.util.HashMap<>();
                                map.put(myUid, meId);
                                map.put(otherUid, otherId);
                                patch.put("uidToId", map);
                                patch.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                                d.getReference().set(patch, com.google.firebase.firestore.SetOptions.merge());
                            }
                        }
                    } finally {
                        postMain(onDone);
                    }
                }))
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(ctx, "Sync friendships fail: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    postMain(onDone);
                });
    }

    private static void postMain(@androidx.annotation.Nullable Runnable r) {
        if (r == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(r);
    }

}
