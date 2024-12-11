package com.coratory.urlretriever;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 
 * @author MorozovR
 */
public class RestrictedCapacityDeque<E> extends LinkedBlockingDeque<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8436715731204746980L;

	public RestrictedCapacityDeque(int capacity) {
		super(capacity);
	}

	@Override
	public void addFirst(E e) {

		if (remainingCapacity() == 0) {
			removeLast();
		}
		super.addFirst(e);
	}

}