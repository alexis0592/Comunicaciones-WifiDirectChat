package co.edu.udea.comunicacionesapp.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;
import co.edu.udea.comunicacionesapp.activities.WifiDirectActivity;

public class ChatManager implements Runnable {

	private static final String TAG = "ChatHandler";

	private Socket socket = null;
	private Handler handler;
	private InputStream inputStream;
	private OutputStream outputStream;

	public ChatManager(Socket socket, Handler handler) {
		this.socket = socket;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			byte[] buffer = new byte[1024];
			int bytes;
			handler.obtainMessage(WifiDirectActivity.MY_HANDLE, this)
					.sendToTarget();

			while (true) {
				try {
					// Leyendo desde el InputStream
					bytes = inputStream.read(buffer);
					if (bytes == -1) {
						break;
					}

					// Enviar los bytes obtenidos a la interfaz
					Log.d(TAG, "Rec:" + String.valueOf(buffer));
					handler.obtainMessage(WifiDirectActivity.MESSAGE_READ,
							bytes, -1, buffer).sendToTarget();

				} catch (IOException io) {
					Log.e(TAG, "desconectado");
				}
			}

		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}

	}

	public void write(byte[] buffer){
		try {
			outputStream.write(buffer);
		} catch (Exception e) {
			Log.e(TAG, "Excepción durante la escritura: " + e);
		}
	}
}
