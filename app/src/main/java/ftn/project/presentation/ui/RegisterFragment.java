package ftn.project.presentation.ui;

import static android.content.ContentValues.TAG;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

import ftn.project.R;


public class RegisterFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText etEmail, etPass, etConfirm, etUsername;
    private Spinner spAvatar;
    private Button btnRegister, btnGoLogin;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = view.findViewById(R.id.etEmail);
        etPass = view.findViewById(R.id.etPass);
        etConfirm = view.findViewById(R.id.etConfirm);
        etUsername = view.findViewById(R.id.etUsername);
        spAvatar = view.findViewById(R.id.spAvatar);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnGoLogin = view.findViewById(R.id.btnGoLogin);

        // popuni spinner sa avatarima
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"WARRIOR", "MAGE", "ROGUE", "HEALER", "RANGER"}
        );
        spAvatar.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> doRegister());

        btnGoLogin.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void doRegister(){
        String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString();
        String confirm = etConfirm.getText().toString();
        String username = etUsername.getText().toString().trim();
        String avatar = spAvatar.getSelectedItem().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirm) || TextUtils.isEmpty(username)) {
            Toast.makeText(getContext(), "Popunite sva polja", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(getContext(), "Lozinke se ne poklapaju", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 3) {
            Toast.makeText(getContext(), "Korisničko ime mora imati bar 3 karaktera", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task ->  {

                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = auth.getCurrentUser();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }
}
