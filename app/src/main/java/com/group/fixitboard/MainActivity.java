package com.group.fixitboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            if (currentUser.getEmail().toLowerCase().startsWith("admin")) {
                startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
            } else {
                Intent intent = new Intent(MainActivity.this, TenantDashboardActivity.class);
                intent.putExtra("TENANT_NAME", TenantLoginActivity.resolveTenantDisplayName(currentUser, currentUser.getEmail()));
                startActivity(intent);
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Button btnTenant = findViewById(R.id.btnTenant);
        Button btnAdmin = findViewById(R.id.btnAdmin);

        btnTenant.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TenantLoginActivity.class)));

        btnAdmin.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AdminLoginActivity.class)));
    }
}