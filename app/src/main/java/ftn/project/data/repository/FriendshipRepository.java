package ftn.project.data.repository;

import android.content.Context;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executors;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Converters;
import ftn.project.domain.entity.Friendship;
import ftn.project.domain.entity.User;

public class FriendshipRepository {
    private final FirebaseFirestore fs;
    private final FirebaseAuth auth;
    private final AppDatabase db;

    public FriendshipRepository(Context ctx) {
        this.fs = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.db = AppDatabase.getInstance(ctx);
    }

    // helper: deterministički ID dokumenta (sprečava duplikate A_B i B_A)
    private String pairId(String a, String b) {
        return (a.compareTo(b) < 0) ? (a + "_" + b) : (b + "_" + a);
    }

    // helper: mapiraj firebaseUid -> lokalni int userId (kreira stub ako ne postoji)
    private int resolveOrInsertByUid(String firebaseUid) {
        User u = db.userRepository().getByFirebaseUid(firebaseUid);
        return u.getUserId();
    }

    public interface Callback {
        void onSuccess();

        void onError(Exception e);
    }

    /**
     * Pozovi kad korisnik klikne "Dodaj prijatelja" ili posle skeniranja QR-a.
     */
    public void sendFriendRequest(String otherUid, Callback cb) {
        if (auth.getCurrentUser() == null) {
            cb.onError(new IllegalStateException("Nije prijavljen korisnik."));
            return;
        }
        String meUid = auth.getCurrentUser().getUid();
        if (meUid.equals(otherUid)) {
            cb.onError(new IllegalArgumentException("Ne možeš poslati zahtev samom sebi."));
            return;
        }

        String docId = pairId(meUid, otherUid);

        // 1) Remote (Firestore) – upiši/azuriraj zahtev u PENDING
        var data = new java.util.HashMap<String, Object>();
        data.put("requesterUid", meUid);
        data.put("addresseeUid", otherUid);
        data.put("status", "PENDING");

        fs.collection("friend_requests")
                .document(docId)
                .set(data)
                .addOnSuccessListener(v -> {
                    // 2) Lokalno (Room) – upiši PENDING (optimistic UI)
                    Executors.newSingleThreadExecutor().execute(() -> {
                        int meId = resolveOrInsertByUid(meUid);
                        int otherId = resolveOrInsertByUid(otherUid);

                        Friendship f = new Friendship();
                        f.firstUserId = meId;
                        f.secondUserId = otherId;

                        db.friendshipRepository().upsert(f);
                        // obavesti UI
                        cb.onSuccess();
                    });
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Prihvati zahtev (poziva ga addressee).
     */
    public void acceptRequest(String requesterUid, Callback cb) {
        if (auth.getCurrentUser() == null) {
            cb.onError(new IllegalStateException("Nije prijavljen korisnik."));
            return;
        }
        String meUid = auth.getCurrentUser().getUid();
        String docId = pairId(requesterUid, meUid);

        fs.collection("friend_requests")
                .document(docId)
                .update("status", "ACCEPTED")
                .addOnSuccessListener(v -> {
                    // refresh lokalno iz Firestore (pull) ili odmah prepiši lokalnu vezu u ACCEPTED
                    Executors.newSingleThreadExecutor().execute(() -> {
                        int meId = resolveOrInsertByUid(meUid);
                        int otherId = resolveOrInsertByUid(requesterUid);

                        Friendship f = new Friendship();
                        f.firstUserId = otherId;   // requester = onaj koji je poslao zahtev
                        f.secondUserId = meId;

                        db.friendshipRepository().upsert(f);
                        cb.onSuccess();
                    });
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Povuci sve moje zahteve iz Firestore i uskladi Room (pozovi na login/otvaranje ekrana).
     */
    public void refreshMyFriendships(Callback cb) {
        if (auth.getCurrentUser() == null) {
            cb.onError(new IllegalStateException("Nije prijavljen korisnik."));
            return;
        }
        String meUid = auth.getCurrentUser().getUid();

        var q1 = fs.collection("friend_requests").whereEqualTo("requesterUid", meUid).get();
        var q2 = fs.collection("friend_requests").whereEqualTo("addresseeUid", meUid).get();

        Tasks.whenAllSuccess(q1, q2).addOnSuccessListener(list -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                int meId = resolveOrInsertByUid(meUid);

                // počisti sve moje relacije pa upiši sveže (najjednostavniji full refresh)
                db.friendshipRepository().deleteAllForUser(meId);

                // helper za insert iz snapshot-a
                java.util.function.Consumer<com.google.firebase.firestore.QuerySnapshot> ins = qs -> {
                    for (var d : qs) {
                        String requesterUid = d.getString("requesterUid");
                        String addresseeUid = d.getString("addresseeUid");
                        String status = d.getString("status");

                        int reqId = resolveOrInsertByUid(requesterUid);
                        int accId = resolveOrInsertByUid(addresseeUid);

                        Friendship f = new Friendship();
                        f.firstUserId = reqId;
                        f.secondUserId = accId;
                        db.friendshipRepository().upsert(f);
                    }
                };

                ins.accept((com.google.firebase.firestore.QuerySnapshot) list.get(0));
                ins.accept((com.google.firebase.firestore.QuerySnapshot) list.get(1));

                cb.onSuccess();
            });
        }).addOnFailureListener(cb::onError);
    }
}
