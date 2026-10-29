package br.com.assinarpdf;

import android.app.Activity;
import android.content.Intent;
import android.util.Base64;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.OutputStream;

@CapacitorPlugin(name = "PdfIntent")
public class PdfIntentPlugin extends Plugin {

  public static String pendingName = null;
  public static String pendingData = null;

  // Entrega à página o PDF que o Android pediu para abrir (uma vez só)
  @PluginMethod
  public void get(PluginCall call) {
    JSObject ret = new JSObject();
    ret.put("name", pendingName == null ? "" : pendingName);
    ret.put("data", pendingData == null ? "" : pendingData);
    pendingName = null;
    pendingData = null;
    call.resolve(ret);
  }

  // Abre a janela "Salvar como" do Android (nome e pasta à escolha)
  @PluginMethod
  public void save(PluginCall call) {
    String data = call.getString("data");
    if (data == null || data.isEmpty()) {
      call.reject("sem dados");
      return;
    }
    String name = call.getString("name", "documento.pdf");
    Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
    i.addCategory(Intent.CATEGORY_OPENABLE);
    i.setType("application/pdf");
    i.putExtra(Intent.EXTRA_TITLE, name);
    startActivityForResult(call, i, "saveResult");
  }

  @ActivityCallback
  private void saveResult(PluginCall call, ActivityResult result) {
    if (call == null) return;
    if (
      result.getResultCode() != Activity.RESULT_OK ||
      result.getData() == null ||
      result.getData().getData() == null
    ) {
      call.reject("cancelado");
      return;
    }
    try {
      byte[] bytes = Base64.decode(call.getString("data"), Base64.DEFAULT);
      OutputStream os = getContext().getContentResolver().openOutputStream(result.getData().getData());
      if (os == null) {
        call.reject("erro ao abrir o destino");
        return;
      }
      os.write(bytes);
      os.close();
      JSObject r = new JSObject();
      r.put("uri", result.getData().getData().toString());
      call.resolve(r);
    } catch (Exception e) {
      call.reject("erro: " + e.getMessage());
    }
  }
}
