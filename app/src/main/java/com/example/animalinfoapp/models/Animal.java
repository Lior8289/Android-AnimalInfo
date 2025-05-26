package com.example.animalinfoapp.models;

import com.google.gson.annotations.SerializedName;

public class Animal {

    // שדות לאנגלית
    @SerializedName("name-en")
    private String nameEn;
    @SerializedName("description-en")
    private String descriptionEn;
    @SerializedName("place_of_found-en")
    private String placeOfFoundEn;

    // שדות לעברית
    @SerializedName("name-he")
    private String nameHe;
    @SerializedName("description-he")
    private String descriptionHe;
    @SerializedName("place_of_found-he")
    private String placeOfFoundHe;

    // --------------------------------------------
    // 1) שדה חדש עבור שם התמונה ב-drawable
    // --------------------------------------------
    @SerializedName("imageName")
    private String imageName;

    // דרוש לפיירבייס
    public Animal() {
    }

    // בנאי מלא - לא חובה, אבל נוח לעתים
    public Animal(String nameEn, String descriptionEn, String placeOfFoundEn,
                  String nameHe, String descriptionHe, String placeOfFoundHe) {
        this.nameEn = nameEn;
        this.descriptionEn = descriptionEn;
        this.placeOfFoundEn = placeOfFoundEn;
        this.nameHe = nameHe;
        this.descriptionHe = descriptionHe;
        this.placeOfFoundHe = placeOfFoundHe;
    }

    // גטרים וסטרים לשדות הקיימים
    public String getNameEn() {
        return nameEn;
    }
    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }
    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getPlaceOfFoundEn() {
        return placeOfFoundEn;
    }
    public void setPlaceOfFoundEn(String placeOfFoundEn) {
        this.placeOfFoundEn = placeOfFoundEn;
    }

    public String getNameHe() {
        return nameHe;
    }
    public void setNameHe(String nameHe) {
        this.nameHe = nameHe;
    }

    public String getDescriptionHe() {
        return descriptionHe;
    }
    public void setDescriptionHe(String descriptionHe) {
        this.descriptionHe = descriptionHe;
    }

    public String getPlaceOfFoundHe() {
        return placeOfFoundHe;
    }
    public void setPlaceOfFoundHe(String placeOfFoundHe) {
        this.placeOfFoundHe = placeOfFoundHe;
    }

    // --------------------------------------------
    // 2) גטר וסטר לשדה החדש imageName
    // --------------------------------------------
    public String getImageName() {
        return imageName;
    }
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}