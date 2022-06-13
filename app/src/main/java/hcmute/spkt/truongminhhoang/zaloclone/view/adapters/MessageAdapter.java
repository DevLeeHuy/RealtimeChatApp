package hcmute.spkt.truongminhhoang.zaloclone.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import hcmute.spkt.truongminhhoang.zaloclone.R;
import hcmute.spkt.truongminhhoang.zaloclone.services.model.Chats;
import hcmute.spkt.truongminhhoang.zaloclone.utils.ImageConvert;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private static final int MSG_TYPE_LEFT_RECEIVED = 0;
    private static final int MSG_TYPE_RIGHT_RECEIVED = 1;
    private ArrayList<Chats> chatArrayList;
    private Context context;
    private String currentUser_sender;

    public MessageAdapter(ArrayList<Chats> chatArrayList, Context context, String currentUser_sender) {
        this.chatArrayList = chatArrayList;
        this.context = context;
        this.currentUser_sender = currentUser_sender;
    }


    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT_RECEIVED) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right_sent, parent, false);
            return new MessageHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left_received, parent, false);
            return new MessageHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {

        Chats chats = chatArrayList.get(position);
        String message = chats.getMessage();
        String type = chats.getType();
        String timeStamp = chats.getTimestamp();
        boolean isSeen = chats.getSeen();
        long intTimeStamp = Long.parseLong(timeStamp);
        String time_msg_received = timeStampConversionToTime(intTimeStamp);
        holder.tv_time.setText(time_msg_received);
        if (type.equals("text")) {
            holder.tv_msg.setPadding(14, 14, 14, 14);
            holder.tv_msg.setText(message);
        } else if (type.equals("image")) {
            holder.iv_chat_image_received.getLayoutParams().height = 400;
            holder.iv_chat_image_received.setImageBitmap(ImageConvert.convertBase64ToBitmap(message));
        } else if (type.equals("audio")) {
            holder.iv_chat_image_received.getLayoutParams().height = 150;
            holder.iv_chat_image_received.getLayoutParams().width = 150;
            holder.iv_chat_image_received.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
            android.media.MediaPlayer mediaPlayer= new android.media.MediaPlayer();

            holder.iv_chat_image_received.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer.isPlaying()) {
                        Log.d("Test", "is playing");
                        holder.iv_chat_image_received.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                        mediaPlayer.stop();
                        mediaPlayer.reset();

                    } else {
                        holder.iv_chat_image_received.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                        String url = "data:audio/mp3;base64," + message;
                        Log.d("Test", "not playing "+url);

                        try {
                            mediaPlayer.setDataSource(url);
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setVolume(300f, 300f);
                            mediaPlayer.setLooping(false);
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(context.getApplicationContext(), "You might not set the DataSource correctly!", Toast.LENGTH_LONG).show();
                        } catch (SecurityException e) {
                            Toast.makeText(context.getApplicationContext(), "You might not set the DataSource correctly!", Toast.LENGTH_LONG).show();
                        } catch (IllegalStateException e) {
                            Toast.makeText(context.getApplicationContext(), "You might not set the DataSource correctly!", Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mediaPlayer.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(android.media.MediaPlayer player) {
                                player.start();
                            }
                        });
                        mediaPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(android.media.MediaPlayer mp) {
                                mediaPlayer.stop();
                                mediaPlayer.reset();
                                holder.iv_chat_image_received.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                            }
                        });

                    }

                }
            });
        }

        if (position == chatArrayList.size() - 1) {
            if (isSeen) {
                holder.tv_seen.setVisibility(View.VISIBLE);
                String seen = "Seen";
                holder.tv_seen.setText(seen);
            } else {
                holder.tv_seen.setVisibility(View.VISIBLE);
                String delivered = "Delivered";
                holder.tv_seen.setText(delivered);
            }
        } else {
            holder.tv_seen.setVisibility(View.GONE);
        }
    }

    public String timeStampConversionToTime(long timeStamp) {

        Date date = new Date(timeStamp);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat jdf = new SimpleDateFormat("hh:mm a");
        jdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return jdf.format(date);

    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        TextView tv_msg;
        TextView tv_time;
        TextView tv_seen;
        ImageView iv_chat_image_received;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            tv_msg = itemView.findViewById(R.id.tv_chat_received);
            tv_time = itemView.findViewById(R.id.tv_chat_time_received);
            tv_seen = itemView.findViewById(R.id.tv_seen);
            iv_chat_image_received = itemView.findViewById(R.id.iv_chat_image_received);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (chatArrayList.get(position).getReceiverId().equals(currentUser_sender)) {
            return MSG_TYPE_LEFT_RECEIVED;
        } else return MSG_TYPE_RIGHT_RECEIVED;
    }
}
