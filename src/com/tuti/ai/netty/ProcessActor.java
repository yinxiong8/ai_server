package com.tuti.ai.netty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.python.util.PythonInterpreter;

public class ProcessActor implements RealtimeProcessInterface {

	static String command[] = new String[] { "/Users/yinxiong/miniconda2/bin/python", "/Users/yinxiong/PycharmProjects/tuti-ml/generate_midi.py", "-d",
			"/Users/yinxiong/PycharmProjects/tuti-ml/data/wav/", "baseline", "bin_3" };

	// static String command[] = new String[] { "/Users/yinxiong/command.sh" };

	/**
	 * @param args
	 */
	private RealtimeProcess mRealtimeProcess = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// new ProcessActor().sendCommand();

		try {
			execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static String PATH = "/Library/Python/2.7/site-packages/";

	public static int execute() throws Exception {

		try {
			System.out.println("start");
			Process pr = Runtime.getRuntime().exec(command);

			BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			in.close();
			pr.waitFor();
			System.out.println("end");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("----end-----");
		return 1;

	}

	public void sendCommand() {
		mRealtimeProcess = new RealtimeProcess(this);
		mRealtimeProcess.setDirectory("/Users/yinxiong/PycharmProjects/tuti-ml");
		mRealtimeProcess.setCommand(command);
		try {
			try {
				mRealtimeProcess.start();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(mRealtimeProcess.getAllResult());
	}

	public void onNewStdoutListener(String newStdout) {
		// TODO Auto-generated method stub
		System.out.println("==>STDOUT  >  " + newStdout);

	}

	public void onNewStderrListener(String newStderr) {
		// TODO Auto-generated method stub
		System.out.println("==>STDERR  >  " + newStderr);
	}

	public void onProcessFinish(int resultCode) {
		// TODO Auto-generated method stub
		System.out.println("==>RESULT_CODE  >  " + resultCode);
	}
}
