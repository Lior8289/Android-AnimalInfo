package com.example.animalinfoapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.animalinfoapp.models.Animal;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;

public class AddAnimalFragment extends Fragment {

    private EditText etName, etDescription, etPlaceOfFound;
    private Button btnAdd, btnBack ,btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_animal, container, false);

        etName         = view.findViewById(R.id.etAnimalName);
        etDescription  = view.findViewById(R.id.etAnimalDescription);
        etPlaceOfFound = view.findViewById(R.id.etAnimalPlaceOfFound);
        btnAdd         = view.findViewById(R.id.btnAddAnimal);
        btnBack        = view.findViewById(R.id.btnBack);
        btnLogout      = view.findViewById(R.id.btnLogout);

        btnAdd.setOnClickListener(v -> {
            Log.d("ADD", "Add button clicked");
            String name  = etName.getText().toString().trim();
            String desc  = etDescription.getText().toString().trim();
            String place = etPlaceOfFound.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(place)) {
                Toast.makeText(requireContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            // קוראים את השפה הנוכחית
            SharedPreferences appPrefs = requireContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String currentLang = appPrefs.getString("lang", "en");

            // בונים אובייקט חיה עם השדות הרצויים
            Animal newAnimal = new Animal();
            if ("he".equals(currentLang)) {
                newAnimal.setNameHe(name);
                newAnimal.setDescriptionHe(desc);
                newAnimal.setPlaceOfFoundHe(place);
            } else {
                newAnimal.setNameEn(name);
                newAnimal.setDescriptionEn(desc);
                newAnimal.setPlaceOfFoundEn(place);
            }

            // בודקים אם המשתמש מחובר
            SharedPreferences userPrefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String emailOrPhone = userPrefs.getString("email", null);
            if (TextUtils.isEmpty(emailOrPhone)) {
                Toast.makeText(requireContext(), getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show();
                return;
            }

            // שמירה גלובלית בצומת "animals"
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("animals");
            String pushId = ref.push().getKey();
            if (pushId == null) {
                Toast.makeText(requireContext(), "Failed to get push key.", Toast.LENGTH_SHORT).show();
                return;
            }

            ref.child(pushId).setValue(newAnimal)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(), getString(R.string.animal_added), Toast.LENGTH_SHORT).show();
                        // חוזרים לרשימת החיות
                        Navigation.findNavController(view).navigate(R.id.action_addAnimalFragment_to_animalsFragment);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_addAnimalFragment_to_animalsFragment);
        });
        btnLogout.setOnClickListener(v -> {
            // מחיקת נתוני המשתמש מ-SharedPreferences
            SharedPreferences preferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear(); // מוחק את כל הנתונים
            editor.apply();

            // מעבר למסך ההתחברות
            Navigation.findNavController(v).navigate(R.id.action_addAnimalFragment_to_loginFragment);
        });
        return view;
    }
}