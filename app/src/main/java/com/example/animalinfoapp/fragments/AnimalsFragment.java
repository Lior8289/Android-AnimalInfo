package com.example.animalinfoapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalinfoapp.R;
import com.example.animalinfoapp.models.Animal;
import com.example.animalinfoapp.models.AnimalAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    private AnimalAdapter adapter;
    private ArrayList<Animal> animalsList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private Button btnAddAnimal, btnLogout;
    private SearchView searchView;
    private TextView emptyView;
    private View listCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_animals, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAnimals);
        FloatingActionButton btnAddAnimal = view.findViewById(R.id.btnGoToAddAnimal);
        SearchView searchView = view.findViewById(R.id.searchViewAnimals);
        emptyView = view.findViewById(R.id.textViewEmpty);
        listCard = view.findViewById(R.id.listCard);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AnimalAdapter(requireContext(), animalsList);
        recyclerView.setAdapter(adapter);
        adapter.setOnDataChangedListener(this::updateEmptyView);
        updateEmptyView();

        // --- SWIPE TO DELETE WITH UNDO AND GARBAGE ICON ---
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
            Paint paint = new Paint();
            {
                paint.setColor(Color.RED);
            }
            Animal deletedAnimal = null;
            int deletedPosition = -1;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deletedPosition = viewHolder.getAdapterPosition();
                deletedAnimal = adapter.removeItem(deletedPosition);
                // Remove from Firebase as well
                DatabaseReference animalsRef = FirebaseDatabase.getInstance().getReference("animals");
                animalsRef.orderByChild("nameEn").equalTo(deletedAnimal.getNameEn())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    ds.getRef().removeValue();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                Snackbar.make(recyclerView, R.string.deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            adapter.restoreItem(deletedAnimal, deletedPosition);
                            recyclerView.scrollToPosition(deletedPosition);
                            // Restore to Firebase
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("animals");
                            ref.push().setValue(deletedAnimal);
                        })
                        .show();
                updateEmptyView();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / 3;

                // Draw red background
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(),
                        (float) itemView.getLeft() + dX, (float) itemView.getBottom(), paint);

                // Animate icon: scale up as swipe progresses
                if (icon != null) {
                    int iconTop = itemView.getTop() + (int) (height - width) / 2;
                    int iconMargin = (int) ((height - width) / 2);
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = itemView.getLeft() + iconMargin + (int) width;
                    int iconBottom = iconTop + (int) width;

                    float progress = Math.min(1f, Math.abs(dX) / itemView.getWidth());
                    int saveCount = c.save();
                    float scale = 0.7f + 0.3f * progress; // scale from 0.7 to 1.0
                    c.scale(scale, scale, iconLeft + width / 2, iconTop + width / 2);
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    icon.setAlpha((int) (255 * progress));
                    icon.draw(c);
                    c.restoreToCount(saveCount);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
        // --- END SWIPE TO DELETE ---

        // כפתור מעבר למסך "הוסף חיה"
        btnAddAnimal.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.action_animalsFragment_to_addAnimalFragment));

        // מאזינים לשינויים בטקסט בשורת החיפוש
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            listCard.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            listCard.setVisibility(View.VISIBLE);
        }
    }
}
