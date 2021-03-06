/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eastwind.io.support;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A {@link ListenableFuture} whose result may be set by a {@link #set(Object)}
 * or {@link #setException(Throwable)} call. It may also be cancelled.
 *
 * @author Sven Mawson
 * @since 9.0 (in 1.0 as {@code ValueFuture})
 */
public class SettableFuture<V> extends AbstractFuture<V> {

	/**
	 * Sets the value of this future. This method will return {@code true} if
	 * the value was successfully set, or {@code false} if the future has
	 * already been set or cancelled.
	 *
	 * @param value
	 *            the value the future should hold.
	 * @return true if the value was successfully set.
	 */
	@Override
	public boolean set(V value) {
		return super.set(value);
	}

	/**
	 * Sets the future to having failed with the given exception. This exception
	 * will be wrapped in an {@code ExecutionException} and thrown from the
	 * {@code get} methods. This method will return {@code true} if the
	 * exception was successfully set, or {@code false} if the future has
	 * already been set or cancelled.
	 *
	 * @param throwable
	 *            the exception the future should hold.
	 * @return true if the exception was successfully set.
	 */
	@Override
	public boolean setException(Throwable throwable) {
		return super.setException(throwable);
	}

	public V getNow() {
		try {
			return get(-1, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			return null;
		}
	}

	public Throwable getExceptionNow() {
		try {
			get(-1, TimeUnit.MILLISECONDS);
			return null;
		} catch (Exception e) {
			return e;
		}
	}

	public void addListener(Runnable listener, Executor exec) {
		super.addListener(listener, exec);
	}

	public <T> void addListener(final OperationListener<T> listener, final T t, Executor exec) {
		super.addListener(new Runnable() {
			@Override
			public void run() {
				listener.complete(t);
			}
		}, exec);
	}
}
