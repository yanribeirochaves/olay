package com.example.aluno.olay;

import android.Manifest;
import android.app.Dialog;
import android.support.v7.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.aluno.olay.Constants.PICK_IMAGE_REQUEST;
import static com.example.aluno.olay.Constants.READ_WRITE_EXTERNAL;

public class CreateActivity extends AppCompatActivity {

    EditText Data;
    EditText Hora;

    int pYear;
    int pMonth;
    int pDay;

    int pHora;
    int pMin;

    //atribui id para dialogo de data/hora
    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    private File chosenFile;
    private Uri returnUri;

    //DATA
    private DatePickerDialog.OnDateSetListener pDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    pYear = year;
                    pMonth = monthOfYear;
                    pDay = dayOfMonth;
                    updateDisplay();
                }
            };

    private void updateDisplay() {
        Data.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(pDay).append("-")
                        .append(pMonth + 1).append("-")
                        .append(pYear).append(" "));

        //pega data atual pra mandar ao banco
        /*dataBanco=new StringBuilder()
                .append(pYear).append("-")
                .append(pMonth + 1).append("-")
                .append(pDay).append("");*/

    }

    //HORA
    private TimePickerDialog.OnTimeSetListener pTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    pHora = hourOfDay;
                    pMin = minute;
                    updateDisplay1();
                }
            };

    private void updateDisplay1() {
        Hora.setText(
                new StringBuilder()
                        .append(pHora).append(":")
                        .append(pMin).append(":")
                        .append("00"));

        /*horaBanco=new StringBuilder()
                .append(pHora).append(":")
                .append(pMin).append(":")
                .append("00");*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        Data=(EditText) findViewById(R.id.txtData);
        Hora=(EditText) findViewById(R.id.txtHora);

        //BOTÕES CANCELAR E OK
        //botões do actionbar, pos isso estou utlizando o findviewbyid
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_done_cancel, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        onUpload(v);
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        // "Cancel"
                        finish();
                    }
                });

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        // END_INCLUDE (inflate_set_custom_view)

        Data.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        Hora.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });

        final Calendar cal = Calendar.getInstance();
        pYear = cal.get(Calendar.YEAR);
        pMonth = cal.get(Calendar.MONTH);
        pDay = cal.get(Calendar.DAY_OF_MONTH);

        /*dataRealizado=new StringBuilder()
                .append(pYear).append("-")
                .append(pMonth + 1).append("-")
                .append(pDay).append("");

        datarealizado=dataRealizado.toString();*/

        updateDisplay();

        final Calendar hor = Calendar.getInstance();
        pHora = hor.get(Calendar.HOUR_OF_DAY);
        pMin = hor.get(Calendar.MINUTE);
        updateDisplay1();
    }

    public void onChoose(View v) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecione a imagem"), PICK_IMAGE_REQUEST);

    }

    public void onUpload(View v) {

        if (chosenFile == null) {
            Toast.makeText(CreateActivity.this, "Escolha uma imagem antes de enviar.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        final NotificationHelper notificationHelper = new NotificationHelper(this.getApplicationContext());
        notificationHelper.createUploadingNotification();

        ImgurService imgurService = ImgurService.retrofit.create(ImgurService.class);

        EditText name = (EditText) findViewById(R.id.txtNome);
        EditText description = (EditText) findViewById(R.id.txtDescricao);


        final Call<ImageResponse> call =
                imgurService.postImage(
                        name.getText().toString(),
                        description.getText().toString(), "", "",
                        MultipartBody.Part.createFormData(
                                "image",
                                chosenFile.getName(),
                                RequestBody.create(MediaType.parse("image/*"), chosenFile)
                        ));

        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response == null) {
                    notificationHelper.createFailedUploadNotification();
                    return;
                }
                if (response.isSuccessful()) {
                    Toast.makeText(CreateActivity.this, "Enviado com Sucesso !", Toast.LENGTH_SHORT)
                            .show();
                    Log.d("URL da imagem", "http://imgur.com/" + response.body().data.id);
                    notificationHelper.createUploadedNotification(response.body());
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Toast.makeText(CreateActivity.this, "Um erro desconhecido ocorreu.", Toast.LENGTH_SHORT)
                        .show();
                notificationHelper.createFailedUploadNotification();
                t.printStackTrace();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            returnUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), returnUri);

                ImageView imageView = (ImageView) findViewById(R.id.imvFoto);
                imageView.setBackgroundResource(R.drawable.bordas2);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            super.onActivityResult(requestCode, resultCode, data);

            Log.d(this.getLocalClassName(), "Before check");


            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                final List<String> permissionsList = new ArrayList<String>();
                addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE);
                addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (!permissionsList.isEmpty())
                    ActivityCompat.requestPermissions(CreateActivity.this,
                            permissionsList.toArray(new String[permissionsList.size()]),
                            READ_WRITE_EXTERNAL);
                else
                    getFilePath();
            } else {
                getFilePath();
            }
        }
    }

    private void getFilePath() {
        String filePath = DocumentHelper.getPath(this, this.returnUri);
        //Safety check to prevent null pointer exception
        if (filePath == null || filePath.isEmpty()) return;
        chosenFile = new File(filePath);
        Log.d("FilePath", filePath);
    }

    private void addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            shouldShowRequestPermissionRationale(permission);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_EXTERNAL:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CreateActivity.this, "Todas as permissões foram concedidas.", Toast.LENGTH_SHORT)
                            .show();
                    getFilePath();
                } else {
                    Toast.makeText(CreateActivity.this, "Algumas permissões foram negadas.", Toast.LENGTH_SHORT)
                            .show();
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //cria um novo dialog para o picker
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        pDateSetListener,
                        pYear, pMonth, pDay);
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        pTimeSetListener,
                        pHora, pMin, false);
        }
        return null;
    }
}