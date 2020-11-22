package example.com.wordbook_201713068.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.wordbook_201713068.EditVocaActivity;
import example.com.wordbook_201713068.R;
import example.com.wordbook_201713068.common.IntentExtra;
import example.com.wordbook_201713068.model.Vocabulary;

public class VocaAdapter extends RecyclerView.Adapter<VocaAdapter.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 1;

    private List<Vocabulary> mData;

    public VocaAdapter(List<Vocabulary> data) {
        this.mData = data;
    }

    @Override
    public VocaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view;

        switch (viewType) {
            case VIEW_TYPE_ITEM:
                view = LayoutInflater.from(context).inflate(R.layout.viewholder_voca, parent, false);
                return new ViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(VocaAdapter.ViewHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ITEM;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_english)
        TextView englishText;
        @BindView(R.id.text_means)
        TextView meansText;

        private View mView;
        private Context mContext;

        public ViewHolder(View view) {
            super(view);
            this.mView = view;
            this.mContext = view.getContext();

            ButterKnife.bind(this, view);
        }

        protected void setData(final Vocabulary voca) {
            englishText.setText(voca.getEnglish());
            meansText.setText(voca.getMeans());

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(new Intent(mContext, EditVocaActivity.class)
                            .putExtra(IntentExtra.VOCA_ID, voca.getId()));
                }
            });
        }
    }
}
