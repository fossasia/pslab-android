package org.fossasia.pslab.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.fossasia.pslab.R;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlMainAdapter extends RecyclerView.Adapter<ControlMainAdapter.ViewHolder> {
    private String[] mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView tvControlMain1;
        public TextView tvControlMain2;
        public EditText edittextControlMain;
        public Button buttonControlMain1;
        public Button buttonControlMain2;
        public Button buttonControlMain3;
        public SeekBar seekbarControlMain;

        public ViewHolder(View view) {
            super(view);
            mCardView = (CardView) view.findViewById(R.id.cardview_control_main);
            tvControlMain1 = (TextView) view.findViewById(R.id.tv_control_main1);
            tvControlMain2 = (TextView) view.findViewById(R.id.tv_control_main2);
            edittextControlMain = (EditText) view.findViewById(R.id.edittext_control_main);
            buttonControlMain1 = (Button) view.findViewById(R.id.button_control_main1);
            buttonControlMain2 = (Button) view.findViewById(R.id.button_control_main2);
            buttonControlMain3 = (Button) view.findViewById(R.id.button_control_main3);
            seekbarControlMain = (SeekBar) view.findViewById(R.id.seekbar_control_main);
        }

    }

    public ControlMainAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    @Override
    public ControlMainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.control_main_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setIsRecyclable(true);
        holder.tvControlMain1.setText(mDataset[position]);

        final Button buttonControlMain1 = holder.buttonControlMain1;
        final Button buttonControlMain2 = holder.buttonControlMain2;
        Button buttonControlMain3 = holder.buttonControlMain2;

        final SeekBar seekbarControlMain = holder.seekbarControlMain;
        final EditText edittextControlMain = holder.edittextControlMain;

        buttonControlMain1.setEnabled(false);
        buttonControlMain2.setEnabled(false);

        edittextControlMain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")){
                    buttonControlMain1.setEnabled(true);
                    buttonControlMain2.setEnabled(true);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        switch (position) {
            case 0:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Double data = Double.valueOf(edittextControlMain.getText().toString());
                        Double dataDecrement = data - 0.0025;
                        if (dataDecrement < -5.0)
                            dataDecrement = -5.0;
                        else if (dataDecrement > 5.0)
                            dataDecrement = 5.0;
                        seekbarControlMain.setProgress((int)((dataDecrement+5)*10));
                        edittextControlMain.setText(String.valueOf(dataDecrement));
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Double data1 = Double.valueOf(edittextControlMain.getText().toString());
                        Double dataIncrement = data1 + 0.0025;
                        if (dataIncrement < -5.0)
                            dataIncrement = -5.0;
                        else if (dataIncrement > 5.0)
                            dataIncrement = 5.0;
                        seekbarControlMain.setProgress((int)((dataIncrement+5)*10));
                        edittextControlMain.setText(String.valueOf(dataIncrement));
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                seekbarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        double text = progress/10.0 - 5.0;
                        edittextControlMain.setText(String.valueOf(text));

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;
            case 1:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Double data = Double.valueOf(edittextControlMain.getText().toString());
                        Double dataDecrement = data - 0.0025;
                        if (dataDecrement < -3.3)
                            dataDecrement = -3.3;
                        else if (dataDecrement > 3.3)
                            dataDecrement = 3.3;
                        seekbarControlMain.setProgress((int)((dataDecrement+3.3)*15.15));
                        edittextControlMain.setText(String.valueOf(dataDecrement));
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Double data1 = Double.valueOf(edittextControlMain.getText().toString());
                        Double dataIncrement = data1 + 0.0025;
                        if (dataIncrement < -3.3)
                            dataIncrement = -3.3;
                        else if (dataIncrement > 3.3)
                            dataIncrement = 3.3;
                        seekbarControlMain.setProgress((int)((dataIncrement+3.3)*15.15));
                        edittextControlMain.setText(String.valueOf(dataIncrement));
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                seekbarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        double text = progress/15.15 - 3.3;
                        edittextControlMain.setText(String.valueOf(text));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;


            case 2:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Double data = Double.valueOf(edittextControlMain.getText().toString());
                        Double dataDecrement = data - 0.0025;
                        if (dataDecrement < 0.0)
                            dataDecrement = 0.0;
                        else if (dataDecrement > 3.3)
                            dataDecrement = 3.3;
                        seekbarControlMain.setProgress((int)(dataDecrement*30.30));
                        edittextControlMain.setText(String.valueOf(dataDecrement));
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Double data1 = Double.valueOf(edittextControlMain.getText().toString());
                        Double dataIncrement = data1 + 0.0025;
                        if (dataIncrement < 0.0)
                            dataIncrement = 0.0;
                        else if (dataIncrement > 3.3)
                            dataIncrement = 3.3;
                        seekbarControlMain.setProgress((int)(dataIncrement*30.30));
                        edittextControlMain.setText(String.valueOf(dataIncrement));
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                seekbarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        double text = progress/30.30;
                        edittextControlMain.setText(String.valueOf(text));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                break;

            case 3:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Double data = Double.valueOf(edittextControlMain.getText().toString());
                        Double dataDecrement = data - 0.0025;
                        if (dataDecrement < 0.0)
                            dataDecrement = 0.0;
                        else if (dataDecrement > 3.3)
                            dataDecrement = 3.3;


                        edittextControlMain.setText(String.valueOf(dataDecrement));
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Double data1 = Double.valueOf(edittextControlMain.getText().toString());
                        Double dataIncrement = data1 + 0.0025;
                        if (dataIncrement < 0.0)
                            dataIncrement = 0.0;
                        else if (dataIncrement > 3.3)
                            dataIncrement = 3.3;

                        edittextControlMain.setText(String.valueOf(dataIncrement));
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                seekbarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        double text = progress/30.30;
                        edittextControlMain.setText(String.valueOf(text));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;

            case 4:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int data = Integer.valueOf(edittextControlMain.getText().toString());
                        int dataDecrement = data - 1;
                        if (dataDecrement < 10)
                            dataDecrement = 10;
                        else if (dataDecrement > 5000)
                            dataDecrement = 5000;

                        seekbarControlMain.setProgress((int)((dataDecrement-10)/49.9));
                        edittextControlMain.setText(String.valueOf(dataDecrement));
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int data1 = Integer.valueOf(edittextControlMain.getText().toString());
                        int dataIncrement = data1 + 1;
                        if (dataIncrement < 10)
                            dataIncrement = 10;
                        else if (dataIncrement > 5000)
                            dataIncrement = 5000;
                        seekbarControlMain.setProgress((int)((dataIncrement-10)/49.9));
                        edittextControlMain.setText(String.valueOf(dataIncrement));
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                seekbarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        int text = (int)(progress*49.9+10);
                        edittextControlMain.setText(String.valueOf(text));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;
            case 5:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int data = Integer.valueOf(edittextControlMain.getText().toString());
                        int dataDecrement = data - 1;
                        if (dataDecrement < 10)
                            dataDecrement = 10;
                        else if (dataDecrement > 5000)
                            dataDecrement = 5000;
                        seekbarControlMain.setProgress((int)((dataDecrement-10)/49.9));
                        edittextControlMain.setText(String.valueOf(dataDecrement));
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int data1 = Integer.valueOf(edittextControlMain.getText().toString());
                        int dataIncrement = data1 + 1;
                        if (dataIncrement < 10)
                            dataIncrement = 10;
                        else if (dataIncrement > 5000)
                            dataIncrement = 5000;
                        seekbarControlMain.setProgress((int)((dataIncrement-10)/49.9));
                        edittextControlMain.setText(String.valueOf(dataIncrement));
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                seekbarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        int text = (int)(progress*49.9+10);
                        edittextControlMain.setText(String.valueOf(text));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;

            case 6:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int data = Integer.valueOf(edittextControlMain.getText().toString());
                        int dataDecrement = data - 1;
                        if (dataDecrement < 10)
                            dataDecrement = 10;
                        else if (dataDecrement > 5000)
                            dataDecrement = 5000;
                        seekbarControlMain.setProgress((int)((dataDecrement-10)/49.9));
                        edittextControlMain.setText(String.valueOf(dataDecrement));
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int data1 = Integer.valueOf(edittextControlMain.getText().toString());
                        int dataIncrement = data1 + 1;
                        if (dataIncrement < 10)
                            dataIncrement = 10;
                        else if (dataIncrement > 5000)
                            dataIncrement = 5000;
                        seekbarControlMain.setProgress((int)((dataIncrement-10)/49.9));
                        edittextControlMain.setText(String.valueOf(dataIncrement));
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                seekbarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        int text = (int)(progress*49.9+10);
                        edittextControlMain.setText(String.valueOf(text));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}