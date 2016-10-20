package net.ucoz.ksen.flagsquize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "FlagQuiz Activity";

    private static final int FLAGS_IN_QUIZ = 2;    // TODO: 19.10.2016  Add levels

    private List<String> fileNamesList; // файлы с флагами
    private List<String> quizCountriesList; // страны в текущей викторине
    private Set<String> regionsSet; // регионы в текущей викторине
    private String correctAnswer; // правильный ответ
    private int totalGuesses; // кол-во попыток
    private int correctAnswers; //кол-во правильных ответов
    private int guessRows; // кол-во строк с вариантами
    private SecureRandom random; // генератор чисел
    private Handler handler; // обработчик для задержки переключений флагов
    private Animation shakeAnimation; // анимация при неправильном ответе

    private LinearLayout quizLinearLayout; // макет с викториной
    private LinearLayout[] guessLinearLayouts; // массив строк с ответами
    private TextView questionNumberTextView; // номер текущего вопроса
    private ImageView flagImageView; // изображение флага
    private TextView answerTextView; // результат

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        fileNamesList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        // загрузка анимации для неправильных ответов
        // повторяется 3 раза
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);

        // ссылки на компоненты gui
        quizLinearLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4LinearLayout);

        // настройка слушателей
        for(LinearLayout row : guessLinearLayouts){
            for (int i = 0; i < row.getChildCount(); i++) {
                Button button = (Button) row.getChildAt(i);
                button.setOnClickListener(guessButtonListener);
            }
        }

        questionNumberTextView.setText(getString(R.string.question).toCharArray(), 1, FLAGS_IN_QUIZ);
        return view;
    }

    public void updateGuessRows(SharedPreferences sharedPreferences){
        // получить выбранное число вариантов ответов
        String choices = sharedPreferences.getString(MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        // скрываем все кнопки, чтобы отобразить только нужные, не учитывая размеры скрытых
        for (LinearLayout layout: guessLinearLayouts){
            layout.setVisibility(View.GONE);
        }

        // отображаем нужное кол-во кнопок
        for (int i = 0; i < guessRows; i++) {
            guessLinearLayouts[i].setVisibility(View.VISIBLE);
        }
    }

    public void updateRegions(SharedPreferences sharedPreferences){
        regionsSet = sharedPreferences.getStringSet(MainActivity.REGIONS, null);
    }

    public void resetQuiz(){
        // получение имен файлов изображений
        AssetManager assets = getActivity().getAssets();
        fileNamesList.clear();

        // перебираем все нужные регионы и записываем все пути к изображениям (без расширения)
        try{
            for (String region: regionsSet){
                String[] paths = assets.list(region);
                for (String path: paths){
                    fileNamesList.add(path.replace(".png", ""));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading image file", e);
        }

        // сбрасываем все счетчики
        quizCountriesList.clear();
        correctAnswers = 0;
        totalGuesses = 0;
        int flagCounter = 1;
        int numberOfFlags = fileNamesList.size();

        // выбираем случайные флаги для викторины
        while (flagCounter <= FLAGS_IN_QUIZ){
            String fileName = fileNamesList.get(random.nextInt(numberOfFlags));

            if (!quizCountriesList.contains(fileName)){
                quizCountriesList.add(fileName);
                flagCounter++;
            }
        }

        // загружаем первый флаг
        loadNextFlag();
    }

    private void loadNextFlag(){

        correctAnswer = quizCountriesList.remove(0);

        answerTextView.setText("");
        questionNumberTextView.setText(getString(R.string.question, correctAnswers + 1, FLAGS_IN_QUIZ));

        // извлекаем регион
        String region = correctAnswer.substring(0, correctAnswer.indexOf('-'));
        // используем AssetManager для загрузкиизображения
        AssetManager assets = getActivity().getAssets();

        // загрузка изображения
        try(InputStream stream = assets.open(region + "/" + correctAnswer + ".png")) {
            Drawable flag = Drawable.createFromStream(stream, correctAnswer);
            flagImageView.setImageDrawable(flag);
            animate(false);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image", e);
        }

        Collections.shuffle(fileNamesList);

        int guessColumns = guessLinearLayouts[0].getChildCount();

        List<String> guesses = new ArrayList<>();
        guesses.addAll(fileNamesList.subList(0, guessRows * guessColumns));

        for (int row = 0; row < guessRows; row++) {
            for (int column = 0; column < guessColumns; column++) {
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);
                newGuessButton.setText(getCountryName(guesses.get(row * 2 + column)));
            }
        }

        if (!guesses.contains(correctAnswer)){
            int row = random.nextInt(guessRows);
            int column = random.nextInt(guessColumns);
            LinearLayout randomRow = guessLinearLayouts[row];
            String countryName = getCountryName(correctAnswer);
            ((Button)randomRow.getChildAt(column)).setText(countryName);
        }

    }

    private String getCountryName(String fileName){
        return fileName.substring(fileName.indexOf('-') + 1).replace('_', ' ');
    }

    private void animate(boolean animateOut){
        if (correctAnswers == 0)
            return;

        int centerX = (quizLinearLayout.getLeft() + quizLinearLayout.getRight()) / 2;
        int centerY = (quizLinearLayout.getTop() + quizLinearLayout.getBottom()) / 2;
        int radius = Math.max(quizLinearLayout.getWidth(), quizLinearLayout.getHeight());

        Animator animator;

        if (animateOut){
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, radius, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadNextFlag();
                }
            });
        }
        else {
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, 0, radius);
        }

        animator.setDuration(500);
        animator.start();
    }


    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = (Button) v;
            String guess = ((Button) v).getText().toString();
            String answer = getCountryName(correctAnswer);

            ++totalGuesses;

            if (guess.equals(answer)){
                ++correctAnswers;

                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(getResources().getColor(R.color.correct_answer, getContext().getTheme()));

                disableButtons();

                if (correctAnswers == FLAGS_IN_QUIZ){
                    DialogFragment quizResult = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle bundle) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(getString(R.string.results, totalGuesses, (1000 / (double) totalGuesses)));
                            builder.setPositiveButton(getString(R.string.reset_quiz), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resetQuiz();
                                }
                            });
                            return builder.create();
                        }

                    };
                    quizResult.setCancelable(false);
                    quizResult.show(getFragmentManager(), "quiz results");
                }

                else{
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);
                        }
                    }, 1000);
                }
            }
            else {
                flagImageView.startAnimation(shakeAnimation);
                answerTextView.setText(getString(R.string.incorrect_answer));
                answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false);
            }


        }
    };

    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int i = 0; i < guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }
    }
}