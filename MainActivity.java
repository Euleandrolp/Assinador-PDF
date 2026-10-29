package br.com.assinarpdf;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import com.getcapacitor.BridgeActivity;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends BridgeActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    registerPlugin(PdfIntentPlugin.class);
    super.onCreate(savedInstanceState);
    lerIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    lerIntent(intent);
    if (getBridge() != null && getBridge().getWebView() != null) {
      getBridge().getWebView().post(() ->
        getBridge().getWebView().evaluateJavascript("window.checkNativePdf&&window.checkNativePdf()", null)
      );
    }
  }

  // Guarda o PDF recebido ("Abrir com" ou "Compartilhar") para a página buscar
  private void lerIntent(Intent intent) {
    if (intent == null) return;
    Uri uri = null;
    String action = intent.getAction();
    if (Intent.ACTION_VIEW.equals(action)) {
      uri = intent.getData();
    } else if (Intent.ACTION_SEND.equals(action)) {
      uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }
    if (uri == null) return;
    try {
      String nome = "documento.pdf";
      Cursor c = getContentResolver().query(uri, null, null, null, null);
      if (c != null) {
        try {
          int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
          if (idx >= 0 && c.moveToFirst()) nome = c.getString(idx);
        } finally {
          c.close();
        }
      }
      InputStream in = getContentResolver().openInputStream(uri);
      if (in == null) return;
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[65536];
      int n;
      while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
      in.close();
      PdfIntentPlugin.pendingName = nome;
      PdfIntentPlugin.pendingData = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
