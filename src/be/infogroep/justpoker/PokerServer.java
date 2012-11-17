/*
 *	A lot of this code is taken from the wePoker, written by the Ambientalk team 
 */

package be.infogroep.justpoker;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.io.IOException;

import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.UUIDSerializer;


public class PokerServer {
	
	int nextClientID = 0;
	private ConcurrentSkipListMap<Integer, Connection> connections = new ConcurrentSkipListMap<Integer, Connection>();

	
	Runnable serverR = new Runnable() {
		public void run() {
			try {
				Log.d("justPoker - Server", "Creating server");
				Server s = new Server();
				Kryo k = s.getKryo();
				//k.setRegistrationRequired(false); //false is the default
				k.register(UUID.class, new UUIDSerializer());
				s.bind(CommLib.SERVER_PORT);
				s.start();
				s.addListener(new Listener() {
					@Override
					public void connected(Connection c) {
						super.connected(c);
						Log.d("justPoker - Server", "Client connected: " + c.getRemoteAddressTCP());
						addClient(c);
					}
					
					@Override
					public void received(Connection c, Object msg) {
						super.received(c, msg);
						Log.d("justPoker - Server", "Message received");
//						if (msg instanceof FutureMessage) {
//							FutureMessage fm = (FutureMessage) msg;
//							Log.d("justPoker - Server", "Resolving future " + fm.futureId + "(" + CommLib.futures.get(fm.futureId) + ") with value " + fm.futureValue);
//							CommLib.resolveFuture(fm.futureId, fm.futureValue);
//						}
//						if (msg instanceof SetClientParameterMessage) {
//							SetClientParameterMessage cm = (SetClientParameterMessage) msg;
//							Log.d("wePoker - Server", "Got SetIDReplyMessage: "+cm.toString());
//							registerClient(c, cm.nickname, cm.avatar, cm.money);
//							gameLoop.broadcast(cm);
//						}
					}
					
					@Override
					public void disconnected(Connection c) {
						super.disconnected(c);
						Log.d("justPoker - Server", "Client disconnected: " + c);
						removeClient(c);
					}
				});
			} catch (IOException e) {
				Log.e("justPoker - Server", "Server thread crashed", e);
			}
		};
	};
	
	public void start() {		
		Log.d("justPoker - Server", "Starting server and exporter threads...");
		new Thread(serverR).start();
		//new Thread(gameLoop).start();
	}
	
	public void addClient(Connection c) {
		Log.d("wePoker - Server", "Adding client " + c.getRemoteAddressTCP());
		connections.put(nextClientID, c);
		c.sendTCP("Hello world of justPoker");
		nextClientID++;
	}
	
	public void registerClient(Connection c, String nickname, int avatar, int money) {
		for (Integer i : connections.keySet()) {
			if (connections.get(i) == c) {
				//gameLoop.addPlayer(c, i, nickname, avatar, money);
				return;
			}
		}
	}
	
	public void removeClient(Connection c) {
		//Log.d("wePoker - Server", "Client removed: " + c);
		for (Integer i : connections.keySet()) {
			if (connections.get(i) == c) {
				//gameLoop.removePlayer(i);
				connections.remove(i);
				return;
			}
		}
	}
}