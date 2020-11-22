package example.com.wordbook_201713068;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import example.com.wordbook_201713068.common.IntentExtra;
import example.com.wordbook_201713068.database.AppDatabase;
import example.com.wordbook_201713068.model.Vocabulary;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TestActivity extends AppCompatActivity {
    private final static int ANSWER_CNT_MAX = 4;
    private final static int TIME_OF_JUDGMENT_SHOWING = 1000;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.text_english)
    TextView englishText;
    @BindViews({R.id.button_means1, R.id.button_means2, R.id.button_means3, R.id.button_means4})
    List<Button> meansBtns;

    private AppDatabase mDb;
    private Disposable mVocaListDisposable;

    private List<Vocabulary> mTestData;
    private ArrayList<Integer> mWrongVocaIds;
    private Map<Integer, String> mAnswerIdMeanMap = new HashMap<>();
    private int mNowQuestions = -1;

    private Handler mHandler = new Handler();

    private AlertDialog.Builder mDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        ActionBarManager.initBackArrowActionbar(this, toolbar, getString(R.string.action_test));

        mDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                AppDatabase.DATABASE_NAME).build();
        mVocaListDisposable = mDb.vocaDao().selectAll().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(vocaList -> {
                    mTestData = vocaList;
                    for (Vocabulary voca : mTestData) {
                        mAnswerIdMeanMap.put(voca.getId(), voca.getMeans());
                    }
                    Collections.shuffle(mTestData);
                    startTest();
                });
        setUpDialog();
    }

    protected void startTest() {
        mNowQuestions = -1;
        mWrongVocaIds = new ArrayList<>();
        mHandler.post(mSettingNewQuestionRunnable);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mWrongVocaIds = intent.getIntegerArrayListExtra(IntentExtra.VOCA_ID_LIST);
        mTestData = new ArrayList<>();

        mVocaListDisposable = Observable.fromArray(mWrongVocaIds.toArray())
                .flatMap(id -> mDb.vocaDao().findById((int) id))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(voca -> {
                    mTestData.add(voca);

                    if (mTestData.size() == mWrongVocaIds.size()) {
                        startTest();
                    }
                });
    }

    protected boolean isCorrect(String clickedMeans) {
        if (mTestData.get(mNowQuestions).getMeans().equals(clickedMeans)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isEnd() {
        return (mNowQuestions == mTestData.size() - 1) ? true : false;
    }

    private Runnable mSettingNewQuestionRunnable = new Runnable() {
        List<String> mAnswers = new ArrayList<>();
        List<Integer> mVocaIdSet;

        @Override
        public void run() {
            mNowQuestions++;
            englishText.setText(mTestData.get(mNowQuestions).getEnglish());

            setNewAnswers(mTestData.get(mNowQuestions).getMeans());

            setMeanTextsOnBtns();
            changeButtonEnables(true);
        }

        private void setNewAnswers(String realAnswer) {
            mAnswers.clear();
            mAnswers.add(realAnswer);

            mVocaIdSet = new ArrayList<>(mAnswerIdMeanMap.keySet());
            Collections.shuffle(mVocaIdSet);

            int id = 1;
            for (int cnt = 0 ; cnt < ANSWER_CNT_MAX - 1 ; cnt++) {
                if (mTestData.get(mNowQuestions).getId() == id) {
                    id++;
                }
                mAnswers.add(mAnswerIdMeanMap.get(id++));
            }
            Collections.shuffle(mAnswers);
        }

        private void setMeanTextsOnBtns() {
            for (int i = 0 ; i < ANSWER_CNT_MAX ; i++) {
                meansBtns.get(i).setText(mAnswers.get(i));
            }
        }
    };

    protected void goToMainActivity() {
        finish();
    }

    protected void goToTestActivityWithWrongWord() {
        startActivity(new Intent(this, TestActivity.class)
                .putIntegerArrayListExtra(IntentExtra.VOCA_ID_LIST, mWrongVocaIds));
    }

    private void setUpDialog() {
        mDialogBuilder = new AlertDialog.Builder(this);
        mDialogBuilder.setMessage(getString(R.string.msg_test_again_wrong_voca))
                .setTitle(R.string.action_test)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        goToTestActivityWithWrongWord();
                    }
                })
                .setNegativeButton(getString(R.string.action_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        goToMainActivity();
                    }
                });
    }

    private void popUpDialog() {
        mDialogBuilder.show();
    }

    protected void changeButtonEnables(boolean isEnable) {
        for (Button btn : meansBtns) {
            btn.setEnabled(isEnable);
        }
    }

    @OnClick({R.id.button_means1, R.id.button_means2, R.id.button_means3, R.id.button_means4})
    public void onMeansBtnsClicked(Button button) {
        changeButtonEnables(false);

        if(isCorrect(button.getText().toString())) {
            englishText.setText(getString(R.string.msg_correct));
        } else {
            englishText.setText(getString(R.string.msg_wrong));
            mWrongVocaIds.add(mTestData.get(mNowQuestions).getId());
        }

        if(isEnd()) {
            if (mWrongVocaIds.size() == 0) {
                Toast.makeText(TestActivity.this, getString(R.string.msg_all_solved), Toast.LENGTH_SHORT).show();
                goToMainActivity();
            } else {
                popUpDialog();
            }
        } else {
            mHandler.postDelayed(mSettingNewQuestionRunnable, TIME_OF_JUDGMENT_SHOWING);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVocaListDisposable.dispose();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home || id == android.R.id.home) {
            goToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        goToMainActivity();
    }
}
