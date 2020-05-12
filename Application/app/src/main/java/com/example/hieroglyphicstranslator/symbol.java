package com.example.hieroglyphicstranslator;

import android.graphics.Bitmap;

public class symbol {
    private Bitmap mImage;
    private String mFirstText;
    private String mSecText;
    private String mThirdText;

    /**
     * Create a new symbol object.
     **/
    //constructor
    public symbol(Bitmap image ,String highestRankText,
                  String secondHighestText,String ThirdHighestText){
        mImage=image;
        mFirstText=highestRankText;
        mSecText=secondHighestText;
        mThirdText=ThirdHighestText;
    }

    public String getFirstText(){return mFirstText;}

    public String getSecondText(){return mSecText;}

    public String getThirdText(){return mThirdText;}

    public Bitmap getSymboolImage(){return mImage;}


}
