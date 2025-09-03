package ftn.project.presentation.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.AllianceMessage;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.AllianceMessageService;
import ftn.project.presentation.adapter.MessageAdapter;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_ALLIANCE_ID = "ftn.project.EXTRA_ALLIANCE_ID";
    private MessageAdapter adapter;
    private AppDatabase db;
    private int allianceId;
    private ArrayList<AllianceMessage> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView lvMessages = findViewById(R.id.lvMessages);
        TextInputEditText etMessage = findViewById(R.id.etMessage);
        MaterialButton btnSend = findViewById(R.id.btnSend);

        db = AppDatabase.getInstance(getApplicationContext());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User loggedUser = db.userRepository().getByFirebaseUid(firebaseUser.getUid());
        AllianceMessageService allianceMessageService = new AllianceMessageService(this);

        allianceId = getIntent().getIntExtra(EXTRA_ALLIANCE_ID, -1);
//        ArrayList<AllianceMessage> messages = new ArrayList<>(db.allianceMessageRepository().getAllianceMessages(allianceId));
        messages = new ArrayList<>();

        adapter = new MessageAdapter(this, messages, loggedUser.getUserId());
        lvMessages.setAdapter(adapter);

        db.allianceMessageRepository().getAllianceMessages(allianceId)
                .observe(this, list -> {
                    Log.d("CHATTTT", "onChanged, size=" + (list == null ? "null" : list.size()));
                    messages.clear();
                    if (list != null) messages.addAll(list);
                    adapter.notifyDataSetChanged();
                    // opcionalno – skroluj na poslednju poruku
                    lvMessages.post(() -> lvMessages.setSelection(adapter.getCount() - 1));
                });

        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString();
            if (!content.trim().isEmpty()) {
                AllianceMessage message = new AllianceMessage();
                message.setAllianceId(allianceId);
                message.setCreatorUserId(loggedUser.getUserId());
                message.setContent(content);
                message.setCreatorUsername(loggedUser.getUsername());
                message.setSentAt(System.currentTimeMillis());
                Log.w("OOOOOOOO", "allianceId: " + allianceId);

                allianceMessageService.createMessage(message,
                        () -> Toast.makeText(getApplicationContext(), "Messages synced ✔", Toast.LENGTH_SHORT).show()
                ).addOnSuccessListener(messageId -> {
                    message.setMessageId(messageId);
                    Log.w("MID12", "ovaj id mi vraca" + messageId);
                    db.allianceMessageRepository().insert(message);
                });


                etMessage.setText("");
            }
        });

        }

        public void updateMessages(){
            db.allianceMessageRepository().getAllianceMessages(allianceId).observe(this, newMessages -> {
                if (newMessages == null) return;

                messages.clear();                 // očisti staru listu
                messages.addAll(newMessages);     // dodaj sve nove poruke
                adapter.notifyDataSetChanged();   // osveži ListView
            });
        }
}