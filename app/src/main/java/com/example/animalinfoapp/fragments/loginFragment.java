package com.example.animalinfoapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.animalinfoapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.Context.MODE_PRIVATE;

public class loginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;

    public loginFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @NonNull ViewGroup container,
                             @NonNull Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etEmail    = view.findViewById(R.id.editTextEmail);
        etPassword = view.findViewById(R.id.editTextPassword);
        Button btnLogin     = view.findViewById(R.id.btnLogin);
        Button btnRegister  = view.findViewById(R.id.btnGoToRegister);

        btnRegister.setOnClickListener(v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_loginFragment_to_registerFragment);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(getContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // שמירת אימייל בהעדפות מקומיות
                                SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                prefs.edit().putString("email", email).apply();

                                Toast.makeText(getContext(), getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                                // מעבר למסך הראשי (רשימת חיות)
                                Navigation.findNavController(view)
                                        .navigate(R.id.action_loginFragment_to_animalsFragment);
                            }
                        } else {
                            Toast.makeText(getContext(),
                                    getString(R.string.login_failed) + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        return view;
    }
}
