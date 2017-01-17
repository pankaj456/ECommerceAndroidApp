package com.smartbuilders.smartsales.ecommerce.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartbuilders.smartsales.ecommerce.R;
import com.smartbuilders.smartsales.ecommerce.data.ChatMessageDB;
import com.smartbuilders.smartsales.ecommerce.model.ChatMessage;
import com.smartbuilders.synchronizer.ids.model.User;

import java.util.ArrayList;

/**
 * Created by Alberto on 8/4/2016.
 */
public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.ViewHolder> {

    private ArrayList<ChatMessage> mDataset;
    private Context mContext;
    private User mUser;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private View containerLayout;
        private TextView message;
        private TextView created;

        public ViewHolder(View v) {
            super(v);
            containerLayout = v.findViewById(R.id.container_layout);
            message = (TextView) v.findViewById(R.id.message);
            created = (TextView) v.findViewById(R.id.created);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatMessagesAdapter(Context context, ArrayList<ChatMessage> myDataset, User user) {
        mDataset = myDataset;
        mUser = user;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatMessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.message.setText(mDataset.get(position).getMessage());
        holder.created.setText(mDataset.get(position).getCreatedStringFormat());
        if (mDataset.get(position).getSenderChatContactId() == mUser.getServerUserId()) {
            ((LinearLayout.LayoutParams) holder.containerLayout.getLayoutParams()).gravity = Gravity.RIGHT;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.containerLayout.setBackgroundResource(R.drawable.ripple_rounded_corners_chat_message_sent);
            } else {
                holder.containerLayout.setBackgroundResource(R.drawable.shape_selector_chat_message_sent);
            }
        } else {
            ((LinearLayout.LayoutParams) holder.containerLayout.getLayoutParams()).gravity = Gravity.LEFT;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.containerLayout.setBackgroundResource(R.drawable.ripple_rounded_corners_chat_message_received);
            } else {
                holder.containerLayout.setBackgroundResource(R.drawable.shape_selector_chat_message_received);
            }
        }
        holder.containerLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setMessage(mContext.getString(R.string.delete_message))
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String result = (new ChatMessageDB(mContext, mUser))
                                        .deactiveMessage(mDataset.get(holder.getAdapterPosition()).getId());
                                if (result==null) {
                                    mDataset.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                } else {
                                    Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataset!=null) {
            return mDataset.size();
        }
        return 0;
    }

    public void addChatMessage(ChatMessage chatMessage) {
        mDataset.add(chatMessage);
        notifyItemInserted(mDataset.indexOf(chatMessage));
    }
}