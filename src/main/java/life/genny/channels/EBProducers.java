package life.genny.channels;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.MessageProducer;

public class EBProducers {
	
	private static MessageProducer<JsonObject> toCmds;
	private static MessageProducer<JsonObject> toData;

	/**
	 * @return the toData
	 */
	public static MessageProducer<JsonObject> getToData() {
		return toData;
	}


	/**
	 * @param toData the toData to set
	 */
	public static void setToData(MessageProducer<JsonObject> toData) {
		EBProducers.toData = toData;
	}


	/**
	 * @return the toCmds
	 */
	public static MessageProducer<JsonObject> getToCmds() {
		return toCmds;
	}
	

	/**
	 * @param toCmds the toCmds to set
	 */
	public static void setToCmds(MessageProducer<JsonObject> toCmds) {
		EBProducers.toCmds = toCmds;
	}
	
	public static void registerAllProducers(EventBus eb){
		setToCmds(eb.publisher("cmds"));
		setToData(eb.publisher("data"));
	}
}
