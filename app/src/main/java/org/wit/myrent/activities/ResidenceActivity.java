package org.wit.myrent.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;

import org.wit.android.helpers.ContactHelper;
import org.wit.myrent.R;
import org.wit.myrent.app.MyRentApp;
import org.wit.myrent.models.Portfolio;
import org.wit.myrent.models.Residence;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.view.View;

import static org.wit.android.helpers.IntentHelper.navigateUp;

import static org.wit.android.helpers.ContactHelper.getContact;
import static org.wit.android.helpers.IntentHelper.selectContact;
import static org.wit.android.helpers.ContactHelper.sendEmail;


import android.content.Intent;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class ResidenceActivity extends AppCompatActivity implements TextWatcher,
    CompoundButton.OnCheckedChangeListener,
    View.OnClickListener,
    DatePickerDialog.OnDateSetListener


{
  private static final int REQUEST_CONTACT = 1;

  private EditText geolocation;
  private Residence residence;

  private CheckBox rented;
  private Button dateButton;
  private Button tenantButton;
  private Button reportButton;

  private Portfolio portfolio;

  private String emailAddress = "";
  /**
   * ATTENTION: This was auto-generated to implement the App Indexing API.
   * See https://g.co/AppIndexing/AndroidStudio for more information.
   */
  private GoogleApiClient client;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_residence);

    geolocation = (EditText) findViewById(R.id.geolocation);
    residence = new Residence();

    // Register a TextWatcher in the EditText geolocation object
    geolocation.addTextChangedListener(this);

    dateButton = (Button) findViewById(R.id.registration_date);
    dateButton.setOnClickListener(this);
    rented = (CheckBox) findViewById(R.id.isrented);
    tenantButton = (Button) findViewById(R.id.tenant);
    reportButton = (Button) findViewById(R.id.residence_reportButton);


    MyRentApp app = (MyRentApp) getApplication();
    portfolio = app.portfolio;
    Long resId = (Long) getIntent().getExtras().getSerializable("RESIDENCE_ID");
    residence = portfolio.getResidence(resId);
    if (residence != null) {
      updateControls(residence);
    }
    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
  }

  public void updateControls(Residence residence) {
    geolocation.setText(residence.geolocation);
    rented.setOnCheckedChangeListener(this);

    dateButton.setText(residence.getDateString());
    tenantButton.setOnClickListener(this);
    reportButton.setOnClickListener(this);


  }

  @Override
  public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

  }

  @Override
  public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

  }

  @Override
  public void afterTextChanged(Editable editable) {
    residence.setGeolocation(editable.toString());
  }

  @Override
  public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
    Log.i(this.getClass().getSimpleName(), "rented Checked");
    residence.rented = isChecked;
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.registration_date:
        Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
        break;

      case R.id.tenant:
        selectContact(this, REQUEST_CONTACT);
        break;

      case R.id.residence_reportButton:
        if (emailAddress == null) {
          emailAddress = "unable to read email address";
        }
        sendEmail(this, emailAddress, getString(R.string.residence_report_subject), residence.getResidenceReport(this));
        break;
    }
  }

  @Override
  public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    Date date = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
    residence.date = date.getTime();
    dateButton.setText(residence.getDateString());
  }

  @Override
  public void onPause() {
    super.onPause();
    portfolio.saveResidences();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        navigateUp(this);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != Activity.RESULT_OK) {
      return;
    }

    switch (requestCode) {
      case REQUEST_CONTACT:
        checkContactsReadPermission();
        String name = ContactHelper.getContact(this, data);
        emailAddress = ContactHelper.getEmail(this, data);
        tenantButton.setText(name + " : " + emailAddress);
        residence.tenant = name;
        break;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case REQUEST_CONTACT: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          // permission was granted. Do the
          // contacts-related task you need to do.
        }
        else {
          // permission denied, boo! Disable the
          // functionality that depends on this permission.
        }
        return;
      }
      // other 'case' lines to check for other
      // permissions this app might request
    }
  }

  public void checkContactsReadPermission() {
    // Here, this is the current activity
    if (ContextCompat.checkSelfPermission(this,
        Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED) {

      // Should we checkContactsReadPermissione show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(this,
          Manifest.permission.READ_CONTACTS)) {
        // Show an expanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.

      }
      else {
        // No explanation needed, we can request the permission.
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.READ_CONTACTS},
            REQUEST_CONTACT);
        // REQUEST_CONTACT is an app-defined int constant.
        // The callback method, onRequestPermissionsResult, gets the result of the request.
      }
    }
  }

}