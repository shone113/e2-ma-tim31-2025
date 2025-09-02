package ftn.project.data.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.Friendship;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;

public class FirestoreSync {
    private FirestoreSync() {}

    public static void mirrorUserEquipmentToFirestore(
            Context ctx, String uid, int userId, UserEquipment ue) {

        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("userEquipmentId", ue.getUserEquipmentId());
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

    public static void syncAllAlliancesDown(Context ctx, AppDatabase db, Runnable onDone) {
        FirebaseFirestore.getInstance()
                .collection("alliances")
                .get()
                .addOnSuccessListener(snaps -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        int count = 0;
                        for (var d : snaps) {
                            Long allianceIdL = d.getLong("allianceId");
                            String name = d.getString("name");
                            Long leaderIdL = d.getLong("leaderUserId");

                            if (allianceIdL == null) continue;
                            int allianceId = allianceIdL.intValue();
                            Integer leaderUserId = leaderIdL == null ? null : leaderIdL.intValue();

                            Alliance local = db.allianceRepository().getAlliance(allianceId);
                            if (local == null) {
                                Alliance a = new Alliance();
                                a.setAllianceId(allianceId);
                                a.setName(name);
                                a.setLeaderUserId(leaderUserId);
                                db.allianceRepository().insert(a);
                            } else {
                                boolean changed = false;
                                if (name != null && !name.equals(local.getName())) {
                                    local.setName(name);
                                    changed = true;
                                }
                                if (leaderUserId != null && !leaderUserId.equals(local.getLeaderUserId())) {
                                    local.setLeaderUserId(leaderUserId);
                                    changed = true;
                                }
                                if (changed) db.allianceRepository().update(local);
                            }
                            count++;
                        }
                        if (onDone != null) new Handler(Looper.getMainLooper()).post(onDone);
                        Log.d("FS_SYNC", "Synced alliances: " + count);
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ctx, "Sync alliances fail: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }


    public static void syncUserEquipmentDown(
            Context ctx,
            AppDatabase db,
            String firebaseUid,
            @Nullable Runnable onDone
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

                                db.userEquipmentRepository().removeAllForUser(me.getUserId());
                                // 2) Prođi kroz sve instance iz Firestore-a
                                for (DocumentSnapshot d : snaps) {
                                    Long eqL = d.getLong("equipmentId");
                                    if (eqL == null) { skipped++; continue; }
                                    int equipmentId = eqL.intValue();

                                    Long bcL = d.getLong("battleCount");
                                    Integer battleCount = (bcL == null) ? 0 : bcL.intValue();

                                    Boolean activeB = d.getBoolean("active");
                                    boolean active = (activeB != null) && activeB;

                                    Long ueIdL = d.getLong("userEquipmentId");
                                    Integer userEquipmentId = ueIdL.intValue();
                                    try {
                                        // 3) Učitaj postojeći zapis (po PK: userId+equipmentId)
                                        //    Prilagodi imenu metode tvog DAO-a ako je drugačije:
                                        //    npr. getByUserAndEquipment / get / findOne
                                        UserEquipment existing =
                                                db.userEquipmentRepository().getById(userEquipmentId);

                                        if (existing == null) {
                                            UserEquipment ue = new UserEquipment();
                                            ue.setUserEquipmentId(ue.getUserEquipmentId());
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
                        if (onDone != null) new Handler(Looper.getMainLooper()).post(onDone);
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
            @Nullable Integer otherUserId // može null ako ne znaš još
    ) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        String docId = fs.collection("friendships").document().getId();
        Map<String, Object> uidToId = new HashMap<>();
        uidToId.put(myUid, myUserId);
        if (otherUserId != null) {
            uidToId.put(otherUid, otherUserId);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("firstUserId", myUserId);
        data.put("secondUserId", otherUserId);
        data.put("uidToId", uidToId); // merge-ovaćemo, pa će se popuniti i drugi ID kad ga saznaš
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        fs.collection("friendships").document(docId).set(data)
                .addOnSuccessListener(unused -> {
                    Log.i("FRIENDSHIP", "sent: " + docId);
                })
                .addOnFailureListener(e -> Log.e("FRIENDSHIP", "fail: " + e));
    }

    public static void syncFriendshipsDown(
            Context ctx,
            AppDatabase db,
            String myUid,
            int myUserId,
            @Nullable Runnable onDone
    ) {
        FirebaseFirestore.getInstance()
                .collection("friendships")
                .get()
                .addOnSuccessListener(snaps -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        int count = 0;
                        for (var d : snaps) {
                            Long firstUserId = d.getLong("firstUserId");
                            Long secondUserId = d.getLong("secondUserId");

                            Log.w("IDIIIII", "firstId: " + firstUserId + ", secondId: " + secondUserId);
                            Friendship local = null;
                            int firstId = -1;
                            int secondId = -1;
                            if (firstUserId != null && secondUserId != null) {
                                firstId = Math.toIntExact(firstUserId);
                                secondId = Math.toIntExact(secondUserId);

                                local = db.friendshipRepository().getFriendhipForUsers(firstId, secondId);
                            }

                            if (local == null) {
                                Friendship f = new Friendship();
                                f.setFirstUserId(firstId);
                                f.setSecondUserId(secondId);
                                db.friendshipRepository().insert(f);
                            }
                            count++;
                        }
                        if (onDone != null) new Handler(Looper.getMainLooper()).post(onDone);
                        Log.d("FRIENDSHIPS_SYNC","Synced friendships: "+count);
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ctx, "Sync friendships fail: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private static void postMain(@Nullable Runnable r) {
        if (r == null) return;
        new Handler(Looper.getMainLooper()).post(r);
    }

}
