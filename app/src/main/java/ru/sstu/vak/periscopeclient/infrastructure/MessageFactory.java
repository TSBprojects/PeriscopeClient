package ru.sstu.vak.periscopeclient.infrastructure;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import ru.sstu.vak.periscopeclient.R;

public class MessageFactory {
    private Context context;
    private LinearLayout messages_layout;
    private Point SCREEN_SIZE;
    private ImageLoader imageLoader;

    public MessageFactory(Context context, LinearLayout messages_layout, Point screen_size) {
        this.SCREEN_SIZE = screen_size;
        this.context = context;
        this.messages_layout = messages_layout;
        initializeImageLoader();
    }

    private void showLayout(View view, int duration) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        final int bottomMarginStart = params.bottomMargin; // your start value

        final int bottomMarginEnd = 0; // your start value
        final int topMarginStart = params.topMargin; // your start value
        final int topMarginEnd = convertDpToPixel(5); // your start value

        int s = view.getHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

                params.bottomMargin = bottomMarginStart + (int) ((bottomMarginEnd - bottomMarginStart) * interpolatedTime);
                params.topMargin = topMarginStart + (int) ((topMarginEnd - topMarginStart) * interpolatedTime);
                view.setLayoutParams(params);
            }
        };
        a.setDuration(duration);
        view.startAnimation(a);
    }


    public void addJoinedLabelToScreen(String text, int color) {
        CardView joinedCardVie = createJoinedCardView();
        TextView joinedUserLoginTV = createJoinedUserLoginTextView(text, color);

        joinedCardVie.addView(joinedUserLoginTV);
        messages_layout.addView(joinedCardVie);

        int messageLayoutHeight = getTextViewHeight(text) + convertDpToPixel(10);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) joinedCardVie.getLayoutParams();
        params.bottomMargin = -messageLayoutHeight;
        joinedCardVie.setLayoutParams(params);

        showLayout(joinedCardVie, 300);
        new CountDownTimer(4000, 4000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                joinedCardVie.animate()
                        .setDuration(1000)
                        .alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        messages_layout.removeView(joinedCardVie);
                    }
                });
            }
        }.start();
    }

    private CardView createJoinedCardView() {
        int PXmarginBottom = -convertDpToPixel(27);
        CardView cardView = new CardView(context);
        LinearLayout.LayoutParams cardView_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                );
        cardView_params.setMargins(0, 0, 0, PXmarginBottom);
        cardView.setRadius(convertDpToPixel(3));
        cardView.setCardElevation(0);
        cardView.setLayoutParams(cardView_params);
        return cardView;
    }

    private TextView createJoinedUserLoginTextView(String userLogin, int color) {
        int PXpadding = convertDpToPixel(5);
        TextView newTextView = new TextView(context);
        LinearLayout.LayoutParams newTextView_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        newTextView.setPadding(PXpadding, PXpadding, PXpadding, PXpadding);
        newTextView.setGravity(Gravity.START | Gravity.CENTER);
        newTextView.setBackgroundColor(color);
        newTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        newTextView.setTextSize(12);
        newTextView.setText("@" + userLogin);
        newTextView.setLayoutParams(newTextView_params);
        return newTextView;
    }


    public void addMessageToScreen(String userLogin, String message, String profileImgPath, int userColor) {
        TextView userMessageTV = createUserMessageTextView(message);
        TextView userLoginTV = createUserLoginTextView(userLogin);
        LinearLayout messageBodyLayout = createMessageBodyLayout();
        ImageView profileImage = createProfileImg(profileImgPath,userColor);
        CardView cardView = createCardView();
        LinearLayout messageLayout = createMainLayout();
        messageBodyLayout.addView(userLoginTV);
        messageBodyLayout.addView(userMessageTV);
        cardView.addView(profileImage);
        messageLayout.addView(cardView);
        messageLayout.addView(messageBodyLayout);
        messages_layout.addView(messageLayout);

        int messageLayoutHeight = getTextViewHeight(message) + convertDpToPixel(50);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) messageLayout.getLayoutParams();
        params.bottomMargin = -messageLayoutHeight;
        messageLayout.setLayoutParams(params);

        showLayout(messageLayout, 400);
        new CountDownTimer(4000, 4000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                messageLayout.animate()
                        .setDuration(1000)
                        .alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        messages_layout.removeView(messageLayout);
                    }
                });
            }
        }.start();
    }

    private int getTextViewHeight(String text) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        int px = (int) (scaledDensity * 12);
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(px);
        int messageLayoutHeight = (new StaticLayout(
                text,
                textPaint,
                SCREEN_SIZE.x - convertDpToPixel(171),
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                true)).getHeight();
        return messageLayoutHeight;
    }

    private LinearLayout createMainLayout() {
        //int PXmarginBottom = -convertDpToPixel(40);
        LinearLayout newLinearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams newLinearLayout_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
        newLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        //newLinearLayout_params.setMargins(0, 0, 0, PXmarginBottom);
        newLinearLayout.setLayoutParams(newLinearLayout_params);
        return newLinearLayout;
    }

    private CardView createCardView() {
        CardView cardView = new CardView(context);
        LinearLayout.LayoutParams cardView_params =
                new LinearLayout.LayoutParams(
                        convertDpToPixel(35),
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                );
        cardView.setRadius(convertDpToPixel(3));
        cardView.setCardElevation(0);
        cardView.setLayoutParams(cardView_params);
        return cardView;
    }


    private ImageView createProfileImg(String profileImgPath, int userColor) {
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams imageView_params =
                new LinearLayout.LayoutParams(
                        convertDpToPixel(35),
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
        imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.chat_pic_decor));
        imageView.setColorFilter(userColor);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).imageScaleType(ImageScaleType.EXACTLY).build();
        imageLoader.displayImage(profileImgPath, imageView, defaultOptions);
        imageView.setLayoutParams(imageView_params);
        return imageView;
    }

    private LinearLayout createMessageBodyLayout() {
        int PXpaddingTopBottom = convertDpToPixel(5);
        int PXpaddingLeftRight = convertDpToPixel(8);
        LinearLayout newLinearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams newLinearLayout_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        newLinearLayout.setOrientation(LinearLayout.VERTICAL);
        newLinearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.chat_edittext_decor));
        newLinearLayout_params.setMargins(-PXpaddingTopBottom, 0, 0, 0);
        newLinearLayout.setPadding(PXpaddingLeftRight, PXpaddingTopBottom, PXpaddingLeftRight, PXpaddingTopBottom);
        newLinearLayout.setLayoutParams(newLinearLayout_params);
        return newLinearLayout;
    }

    private TextView createUserLoginTextView(String userLogin) {
        TextView newTextView = new TextView(context);
        LinearLayout.LayoutParams newTextView_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0
                );
        newTextView.setGravity(Gravity.START | Gravity.CENTER);
        newTextView.setBackgroundColor(ContextCompat.getColor(context,android.R.color.white));
        newTextView.setTextColor(ContextCompat.getColor(context, R.color.user_name_color));
        newTextView.setTextSize(11);
        newTextView.setText("@" + userLogin);
        newTextView.setLayoutParams(newTextView_params);
        return newTextView;
    }

    private TextView createUserMessageTextView(String message) {
        int PXmargin = convertDpToPixel(2);
        TextView newTextView = new TextView(context);
        LinearLayout.LayoutParams newTextView_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0
                );
        newTextView.setGravity(Gravity.START | Gravity.CENTER);
        newTextView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        newTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        newTextView.setTextSize(12);
        newTextView_params.setMargins(0, PXmargin, 0, 0);
        newTextView.setText(message);
        newTextView.setLayoutParams(newTextView_params);
        return newTextView;
    }


    private int convertDpToPixel(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private void initializeImageLoader() {
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration.Builder f = new ImageLoaderConfiguration.Builder(context);
        imageLoader.init(f.build());
    }

}
