package com.xsensio.nfcsensorcomm.mainactivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.google.gson.Gson;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.calibration.CalibrationActivity;
import com.xsensio.nfcsensorcomm.files.FileManagerActivity;
import com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration.NfcTagConfigurationContract;
import com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration.NfcTagConfigurationFragment;
import com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration.NfcTagConfigurationIntentService;
import com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm.PhoneMcuCommContract;
import com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm.PhoneMcuCommFragment;
import com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm.PhoneMcuCommPresenter;
import com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommContract;
import com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommFragment;
import com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.PhoneTagCommPresenter;
import com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.SensorCommContract;
import com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.SensorCommFragment;
import com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.SensorCommPresenter;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.ReducedMeasurement;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.settings.SettingsActivity;
import com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration.NfcTagConfigurationPresenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


//TODO there is strange bug happening in NFC communication
// If we disrupt the communication while it's reading from tag, next time the reading process didn't stop at all
// unless we restart nfc tag and app or just reinstall the app to the phone.  /Bilguun/
// The main method of this communication depends on broadcasting data and receiving. For example if you want to read tag
// this code will just broadcast the command, and handlers will read data from tag and broadcast it back. I tried to fix the above
// error but failed miserably. The initial method is somewhat vague and perhaps dangerous to modify. So proceed with care :)
public class MainActivity extends AppCompatActivity implements MainActivityContract.View,
        HomeScreen.OnFragmentInteractionListener,
        LoadingScreen.OnFragmentInteractionListener,
        DataHistoryScreen.OnFragmentInteractionListener,
        ResultScreen.OnFragmentInteractionListener{

    private static final String TAG = "MyActivity";

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FrameLayout mHomeScreen;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;
    public HomeScreen homeScreen=new HomeScreen();
    public LoadingScreen loadingScreen= new LoadingScreen();
    public DataHistoryScreen historyScreen= new DataHistoryScreen();
    public ResultScreen resultScreen= new ResultScreen();

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    private MainActivityContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        // Preload all tabs to make the transition from one tab to another smoother
        mViewPager.setOffscreenPageLimit(3);
        setViewPager(mViewPager, adapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
        } else {

            // Check for available NFC Adapter
            if (!mNfcAdapter.isEnabled()) {
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            }
            Toast.makeText(this, "NFC is available and enabled", Toast.LENGTH_SHORT).show();
        }

        mPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        mHomeScreen=findViewById(R.id.home_screen_container);
        fragmentTransaction=getSupportFragmentManager().beginTransaction();
        changeFragment("homeScreen");

        generateRandomHistoryData(10);
    }

    public void generateRandomHistoryData(int amount){
        clearMeasurements();
        Random generator=new Random();
        LocalDateTime firstDay=LocalDateTime.now().minusDays(amount);
        for (int i = 0; i < amount; i++) {
            addMeasurement(new ReducedMeasurement(
                    firstDay.plusDays(i),
                    generator.nextFloat()*7,
                    generator.nextFloat()*10,
                    generator.nextFloat()*30+15
            ));
        }
    }

    public void changeFragment(String tag){
        fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_in,R.anim.fragment_out);
        currentScreen=tag;
        switch (tag){
            case "homeScreen":
                fragmentTransaction.replace(R.id.home_screen_container,homeScreen);
                break;
            case "loadingScreen":
                fragmentTransaction.replace(R.id.home_screen_container,loadingScreen);
                break;
            case "resultScreen":
                fragmentTransaction.replace(R.id.home_screen_container,resultScreen);
                break;
            case "historyScreen":
                fragmentTransaction.replace(R.id.home_screen_container,historyScreen);
                break;
        }
        fragmentTransaction.commit();
    }

    public void addMeasurement(ReducedMeasurement measurement){
        //SAVING to EXTERNAL
        File external= Environment.getExternalStorageDirectory();
        File myDir=new File(external,"/xsensio");
        if(!myDir.exists()){
            myDir.mkdir(); }
        File file=new File(myDir,"measurements.csv");
        if(!file.exists()){
            String object="date,ph,sodium,temperature\n";
            try {
                file.createNewFile();
                FileOutputStream fos =  new FileOutputStream(file);
                fos.write(object.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fw = new FileWriter(file,true); //the true will append the new data
            fw.write(measurement.toString());//appends the string to the file
            fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    public void clearMeasurements(){
        File external= Environment.getExternalStorageDirectory();
        File myDir=new File(external,"/xsensio");
        if(!myDir.exists()){
            myDir.mkdir(); }
        File file=new File(myDir,"measurements.csv");
        if(file.exists()){
            try {
                new PrintWriter(file).close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<ReducedMeasurement> getMeasurements(){
        ArrayList<ReducedMeasurement> results=new ArrayList<>();
        File external= Environment.getExternalStorageDirectory();
        File myDir=new File(external,"/xsensio");
        if(!myDir.exists()){
            return null; }
        File file=new File(myDir,"measurements.csv");
        if(!file.exists()){
            return null;
        }
        boolean isHeader=true;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                if(isHeader){
                    isHeader=false;
                } else {
                    results.add(new ReducedMeasurement(sCurrentLine));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    public void showHomeScreen(){
        mToolbar.setVisibility(View.GONE);
        mViewPager.setVisibility(View.GONE);
        mTabLayout.setVisibility(View.GONE);
        mHomeScreen.setVisibility(View.VISIBLE);
        currentScreen="homeScreen";
    }

    public void hideHomeScreen(){
        mToolbar.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);
        mTabLayout.setVisibility(View.VISIBLE);
        mHomeScreen.setVisibility(View.GONE);
        currentScreen="main";
    }

    public String currentScreen="homeScreen";
    @Override
    public void onBackPressed() {
        switch (currentScreen) {
            case "main":
                showHomeScreen();
                break;
            case "homeScreen":
                hideHomeScreen();
                break;
            case "loadingScreen":
                changeFragment("homeScreen");
                break;
            case "resultScreen":
                changeFragment("homeScreen");
                break;
            case "historyScreen":
                changeFragment("homeScreen");
                break;
        }
        showHomeScreen();
    }

    public void readSensors(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int numSamplesReadoutsCase1 = Integer.valueOf(settings.getString("num_samples_roc1", getString(R.string.num_samples_roc1_def_val)));
        int numSamplesReadoutsCase2 = Integer.valueOf(settings.getString("num_samples_roc2", getString(R.string.num_samples_roc2_def_val)));
        int numSamplesReadoutsCase3 = Integer.valueOf(settings.getString("num_samples_roc3", getString(R.string.num_samples_roc3_def_val)));
        int sensorSelect = Integer.valueOf(settings.getString("sensor_select","10"));
        int sampleRate = Integer.valueOf(settings.getString("sampling_frequency","5"));
        PhoneMcuCommand command = new PhoneMcuCommand(
                getApplicationContext(),
                true,
                true,
                true,
                false,
                numSamplesReadoutsCase1,
                numSamplesReadoutsCase2,
                numSamplesReadoutsCase3,
                sensorSelect,
                sampleRate
        );
        sensorCommFragment.mPresenter.readSensors(command);
        changeFragment("loadingScreen");
    }

    SensorCommFragment sensorCommFragment;
    private void setViewPager(ViewPager viewPager, ViewPagerAdapter adapter) {

        // Build GUI
        sensorCommFragment = new SensorCommFragment();
        Fragment nfcTagConfigurationFragment = new NfcTagConfigurationFragment();
        Fragment phoneTagCommFragment = new PhoneTagCommFragment();
        Fragment phoneMcuCommFragment = new PhoneMcuCommFragment();

        adapter.addFragment(sensorCommFragment, "Sensors reading");
        adapter.addFragment(nfcTagConfigurationFragment, "Tag Config");
        adapter.addFragment(phoneTagCommFragment, "Phone/Tag Comm");
        adapter.addFragment(phoneMcuCommFragment, "Phone/MCU Comm");

        viewPager.setAdapter(adapter);

        /**
         * Build View and Presenter (MVP)
         */
        ArrayList<CommContract.Presenter> commContractPresenters = new ArrayList<CommContract.Presenter>();

        // Build View and Presenter of SensorComm
        SensorCommContract.View sensorCommView = (SensorCommContract.View) sensorCommFragment;
        SensorCommContract.Presenter sensorCommPresenter = new SensorCommPresenter(this, sensorCommView);
        sensorCommView.setPresenter(sensorCommPresenter);
        commContractPresenters.add(sensorCommPresenter);

        // Build View and Presenter of NfcTagConfiguration
        NfcTagConfigurationContract.View nfcTagConfigView = (NfcTagConfigurationContract.View) nfcTagConfigurationFragment;
        NfcTagConfigurationContract.Presenter nfcTagConfigPresenter = new NfcTagConfigurationPresenter(this, nfcTagConfigView);
        nfcTagConfigView.setPresenter(nfcTagConfigPresenter);
        commContractPresenters.add(nfcTagConfigPresenter);

        // Build View and Presenter of PhoneTagComm
        PhoneTagCommContract.View phoneTagView = (PhoneTagCommContract.View) phoneTagCommFragment;
        PhoneTagCommContract.Presenter phoneTagCommPresenter = new PhoneTagCommPresenter(this, phoneTagView);
        phoneTagView.setPresenter(phoneTagCommPresenter);
        commContractPresenters.add(phoneTagCommPresenter);

        // Build View and Presenter of PhoneMcuComm
        PhoneMcuCommContract.View phoneMcuView = (PhoneMcuCommContract.View) phoneMcuCommFragment;
        PhoneMcuCommContract.Presenter phoneMcuCommPresenter = new PhoneMcuCommPresenter(this, phoneMcuView);
        phoneMcuView.setPresenter(phoneMcuCommPresenter);
        commContractPresenters.add(phoneMcuCommPresenter);

        // Build Presenter of MainActivity (which is the corresponding View)
        mPresenter = new MainActivityPresenter(this, commContractPresenters);
    }

    @Override
    public void setPresenter(MainActivityContract.Presenter presenter) {
        // Done in setViewPager()
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        mPresenter.processReceivedIntent(intent);
    }

    public void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }

        unregisterReceiver((BroadcastReceiver) mPresenter);
    }

    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {

            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC must be enabled to use this app.", Toast.LENGTH_LONG);
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            }
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }

        if (mPresenter != null) {
            registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(NfcTagConfigurationIntentService.ACTION_READ_TAG_CONFIGURATION));
            registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(NfcTagConfigurationIntentService.ACTION_WRITE_TAG_CONFIGURATION));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_calibration:
                intent = new Intent(this, CalibrationActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_files:
                intent = new Intent(this, FileManagerActivity.class);
                intent.putExtra("Exist",sensorCommFragment.mVirtualSensorsRows.size()>0);
                ArrayList<VirtualSensor> tmp=new ArrayList<>();
                tmp.addAll(sensorCommFragment.mVirtualSensorsRows);
                intent.putParcelableArrayListExtra("sensors",tmp);
                startActivityForResult(intent,96);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==96 & resultCode==69){
            ArrayList<VirtualSensor> result=data.getExtras().getParcelableArrayList("sensors");
            sensorCommFragment.updateSensorResult(result);
        }
    }

}