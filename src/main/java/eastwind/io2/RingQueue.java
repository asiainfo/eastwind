package eastwind.io2;

import java.util.Map;

import com.google.common.collect.Maps;

public class RingQueue<T> {

	private Map<T, Node<T>> nodes = Maps.newHashMap();
	private Node<T> current = null;
	private Node<T> head = null;

	public void add(T e) {
		Node<T> node = new Node<T>();
		node.e = e;
		if (head == null) {
			head = node;
			node.pre = node;
			node.next = node;
		} else {
			Node<T> tail = head.pre;
			node.next = head;
			node.pre = tail;
			head.pre = node;
			tail.next = node;
		}
		if (current == null) {
			current = node;
		}
		nodes.put(e, node);
	}

	public T next(T e) {
		if (e == null) {
			e = current.e;
			current = current.next;
			return e;
		}
		return nodes.get(e).next.e;
	}

	static class Node<T> {
		T e;
		Node<T> pre;
		Node<T> next;
	}

}
