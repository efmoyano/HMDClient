package ar.unsta.robotteam.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

public class GuiUtils {

	public static void showMessage(Context p_context, String p_message) {

		Toast.makeText(p_context, p_message, Toast.LENGTH_SHORT).show();
	}

	public static void alertDialogToSettings(final Context p_context,
			String title, String message, String buttonText, final String action) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				p_context);

		alertDialogBuilder.setTitle(title);

		alertDialogBuilder.setMessage(message).setCancelable(false)
				.setPositiveButton(buttonText,

				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						p_context.startActivity(new Intent(action));

					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();

	}

	public static boolean validateTextField(EditText editText,
			String errorMessage) {
		if (editText.getText().toString().length() == 0) {
			editText.setError(errorMessage);
			return false;
		} else {
			return true;
		}
	}

}
