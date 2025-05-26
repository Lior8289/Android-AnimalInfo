package com.example.animalinfoapp.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalinfoapp.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    private final List<Animal> originalList;  // הרשימה המקורית
    private final List<Animal> filteredList;  // רשימה אחרי סינון
    private final String currentLang;

    public AnimalAdapter(Context context, List<Animal> animals) {
        // originalList מצביעה על אותה רשימה שמגיעה מה-Fragment
        this.originalList = animals;
        this.filteredList = new ArrayList<>(animals);

        // קריאת השפה הנוכחית (ברירת מחדל en)
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        currentLang = prefs.getString("lang", "en");
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_animal, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        // מקבלים את האובייקט הנוכחי מתוך הרשימה המסוננת
        Animal animal = filteredList.get(position);

        // קובעים שם, תיאור ומקום-מציאה לפי השפה
        if ("he".equals(currentLang)) {
            holder.nameTextView.setText(animal.getNameHe());
            holder.descriptionTextView.setText(animal.getDescriptionHe());
            holder.placeTextView.setText(animal.getPlaceOfFoundHe());
        } else {
            holder.nameTextView.setText(animal.getNameEn());
            holder.descriptionTextView.setText(animal.getDescriptionEn());
            holder.placeTextView.setText(animal.getPlaceOfFoundEn());
        }

        // -----------------------------------------------------
        // 1) שליפת השם של התמונה מהאובייקט (imageName)
        // -----------------------------------------------------
        String imageName = animal.getImageName();

        // -----------------------------------------------------
        // 2) בדיקה אם imageName ריק או null:
        //    אם כן, נשים תמונה ברירת מחדל (ic_default_image)
        // -----------------------------------------------------
        if (imageName == null || imageName.trim().isEmpty()) {
            holder.animalImageView.setImageResource(R.drawable.default_image);
        } else {
            // יש שם: מנסים למצוא את ה-resource ב-drawable
            int resourceId = holder.itemView.getContext()
                    .getResources()
                    .getIdentifier(
                            imageName,
                            "drawable",
                            holder.itemView.getContext().getPackageName()
                    );

            if (resourceId != 0) {
                holder.animalImageView.setImageResource(resourceId);
            } else {
                // אם לא נמצא תואם ב-drawable, נשתמש בברירת מחדל
                holder.animalImageView.setImageResource(R.drawable.kangaroo);
            }
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // פונקציית הסינון (מחפשים בשמות/תיאורים גם בעברית וגם באנגלית)
    public void filter(String query) {
        query = query.toLowerCase().trim();
        filteredList.clear();

        if (query.isEmpty()) {
            // בלי חיפוש, מציגים הכל
            filteredList.addAll(originalList);
        } else {
            for (Animal a : originalList) {
                String nameHe = (a.getNameHe() == null) ? "" : a.getNameHe().toLowerCase();
                String descHe = (a.getDescriptionHe() == null) ? "" : a.getDescriptionHe().toLowerCase();
                String nameEn = (a.getNameEn() == null) ? "" : a.getNameEn().toLowerCase();
                String descEn = (a.getDescriptionEn() == null) ? "" : a.getDescriptionEn().toLowerCase();

                // אם שם/תיאור בעברית או באנגלית מכילים את הטקסט שמחפשים
                if (nameHe.contains(query) || descHe.contains(query)
                        || nameEn.contains(query) || descEn.contains(query)) {
                    filteredList.add(a);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class AnimalViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, placeTextView;
        ImageView animalImageView;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView        = itemView.findViewById(R.id.animalName);
            descriptionTextView = itemView.findViewById(R.id.animalDescription);
            placeTextView       = itemView.findViewById(R.id.animalPlace);

            // מצביע ל-ImageView ב-layout
            animalImageView     = itemView.findViewById(R.id.animalImage);
        }
    }
}