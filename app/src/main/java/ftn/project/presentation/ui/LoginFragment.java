package ftn.project.presentation.ui;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import ftn.project.R;

public class LoginFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText etEmail, etPass;
    private Button btnLogin, btnGoRegister, btnResend;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = view.findViewById(R.id.etEmail);
        etPass = view.findViewById(R.id.etPass);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoRegister = view.findViewById(R.id.btnGoRegister);
        btnResend = view.findViewById(R.id.btnResend);

        // login click
        btnLogin.setOnClickListener(v -> doLogin());

        // go to register fragment
        btnGoRegister.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.host, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // resend verification
        btnResend.setOnClickListener(v -> resendVerification());

        return view;
    }

    private void doLogin(){
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(getContext(), "Unesite email i lozinku", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                user.reload().addOnCompleteListener(r -> {
                                    if (user.isEmailVerified()) {
                                        Toast.makeText(getContext(), "Uspešna prijava", Toast.LENGTH_SHORT).show();
                                        // ovde možeš pokrenuti HomeActivity
                                        // Intent i = new Intent(getActivity(), HomeActivity.class);
                                        // startActivity(i);
                                        // requireActivity().finish();
                                    } else {
                                        auth.signOut();
                                        Toast.makeText(getContext(),
                                                "Nalog nije aktiviran. Proveri email.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getContext(),
                                    "Pogrešan email ili lozinka",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void resendVerification(){
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(getContext(), "Unesite email i lozinku da biste poslali verifikaciju",
                    Toast.LENGTH_LONG).show();
            return;
        }


    }
}
