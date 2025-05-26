package com.example.animalinfoapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalinfoapp.R;
import com.example.animalinfoapp.models.Animal;
import com.example.animalinfoapp.models.AnimalAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AnimalsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AnimalAdapter adapter;
    private ArrayList<Animal> animalsList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private Button btnAddAnimal, btnLogout;
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_animals, container, false);


        recyclerView = view.findViewById(R.id.recyclerViewAnimals);
        btnAddAnimal = view.findViewById(R.id.btnGoToAddAnimal);
        searchView = view.findViewById(R.id.searchViewAnimals);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // יוצרים את ה-Adapter + מחברים אותו ל-RecyclerView
        adapter = new AnimalAdapter(requireContext(), animalsList);
        recyclerView.setAdapter(adapter);

        // כפתור מעבר למסך "הוסף חיה"
        btnAddAnimal.setOnClickListener(v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_animalsFragment_to_addAnimalFragment);
        });

        // מאזינים לשינויים בטקסט בשורת החיפוש
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // לחיצה על "חפש" במקלדת
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // חיפוש חי (בכל הקלדה)
                adapter.filter(newText);
                return false;
            }
        });

        // טוענים רשימת חיות מ-Firebase (אם יש)
        loadAnimalsFromFirebase();

        // אם אין חיות כלל ב-Firebase, נטען פעם ראשונה מה-JSON
        uploadAnimalsToFirebaseIfEmpty();
        btnLogout      = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // מחיקת נתוני המשתמש מ-SharedPreferences
            SharedPreferences preferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear(); // מוחק את כל הנתונים
            editor.apply();

            // מעבר למסך ההתחברות
            Navigation.findNavController(v).navigate(R.id.action_animalsFragment_to_loginFragment);
        });

        return view;
    }

    private void loadAnimalsFromFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("animals");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                animalsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Animal animal = ds.getValue(Animal.class);
                    if (animal != null) {
                        animalsList.add(animal);
                    }
                }
                // מוודאים שכל הנתונים נטענו ב-Adapter
                // וקוראים ל-filter("") כדי להציג הכל
                adapter.filter("");
                Log.d("Firebase", "Animals loaded: " + animalsList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load animals", error.toException());
            }
        });
    }

    private void uploadAnimalsToFirebaseIfEmpty() {
        databaseReference = FirebaseDatabase.getInstance().getReference("animals");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    Log.d("Firebase", "Animals already exist, skipping upload from JSON.");
                    return;
                }
                // אחרת, אין חיות – נטען מ-JSON
                List<Animal> fromJson = loadAnimalsFromJson();
                if (fromJson == null || fromJson.isEmpty()) {
                    Log.e("Firebase", "No animals found in JSON.");
                    return;
                }
                for (Animal a : fromJson) {
                    databaseReference.push().setValue(a);
                }
                Log.d("Firebase", "Animals uploaded successfully from JSON.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "uploadAnimalsToFirebaseIfEmpty cancelled", error.toException());
            }
        });
    }

    private List<Animal> loadAnimalsFromJson() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.animals);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();
            inputStream.close();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<Animal>>() {}.getType();
            return gson.fromJson(jsonString.toString(), listType);

        } catch (IOException e) {
            Log.e("JSON", "Error reading animals JSON file", e);
            return null;
        }
    }
}
