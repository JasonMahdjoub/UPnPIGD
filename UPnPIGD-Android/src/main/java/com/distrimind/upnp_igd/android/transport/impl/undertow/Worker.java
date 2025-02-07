/*
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.distrimind.upnp_igd.android.transport.impl.undertow;

import org.wildfly.common.context.ContextManager;
import org.wildfly.common.function.*;
import org.xnio.*;
import org.xnio.channels.*;
import org.xnio.management.XnioServerMXBean;
import org.xnio.management.XnioWorkerMXBean;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * @author Jason Mahdjoub
 * @since 1.2.0
 */
public class Worker {
	private static final Xnio xnio = Xnio.getInstance("nio");

	public static XnioWorker createDefaultWorker() {
		try {
			return xnio.createWorker(OptionMap.builder()
					.set(Options.WORKER_IO_THREADS, 1)
					.set(Options.CONNECTION_HIGH_WATER, 1000000)
					.set(Options.CONNECTION_LOW_WATER, 1000000)
					.set(Options.WORKER_TASK_CORE_THREADS, 0)
					.set(Options.WORKER_TASK_MAX_THREADS, Runtime.getRuntime().availableProcessors())
					.set(Options.TCP_NODELAY, true)
					.set(Options.KEEP_ALIVE, true)
					.getMap());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings({"deprecation","NullableProblems"})
	public static XnioWorker createDefaultMockWorker() {

		return new XnioWorker(xnio.createWorkerBuilder()
				.setWorkerIoThreads(1)
				.setCoreWorkerPoolSize(0)
				.setMaxWorkerPoolSize(Runtime.getRuntime().availableProcessors())) {
			final XnioWorker defaultWorker =createDefaultWorker();
			@Override
			public void shutdown() {
				defaultWorker.shutdown();
			}

			@Override
			public List<Runnable> shutdownNow() {
				return defaultWorker.shutdownNow();
			}

			@Override
			public boolean isShutdown() {
				return defaultWorker.isShutdown();
			}

			@Override
			public boolean isTerminated() {
				return defaultWorker.isTerminated();
			}

			@Override
			public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
				return defaultWorker.awaitTermination(timeout, unit);
			}

			@Override
			public void awaitTermination() throws InterruptedException {
				defaultWorker.awaitTermination();
			}

			@Override
			public XnioIoThread getIoThread(int hashCode) {
				return defaultWorker.getIoThread(hashCode);
			}

			@Override
			public int getIoThreadCount() {
				return defaultWorker.getIoThreadCount();
			}

			@Override
			protected XnioIoThread chooseThread() {
				return defaultWorker.getIoThread();
			}

			@Override
			public XnioWorkerMXBean getMXBean() {
				return defaultWorker.getMXBean();
			}

			@Override
			protected ManagementRegistration registerServerMXBean(XnioServerMXBean metrics) {
				return null;
			}


			@Override
			public ContextManager<XnioWorker> getInstanceContextManager() {
				return defaultWorker.getInstanceContextManager();
			}

			@Override
			public AcceptingChannel<StreamConnection> createStreamConnectionServer(SocketAddress bindAddress, ChannelListener<? super AcceptingChannel<StreamConnection>> acceptListener, OptionMap optionMap) throws IOException {
				return defaultWorker.createStreamConnectionServer(bindAddress, acceptListener, optionMap);
			}



			@Deprecated
			@Override
			public AcceptingChannel<? extends ConnectedStreamChannel> createStreamServer(SocketAddress bindAddress, ChannelListener<? super AcceptingChannel<ConnectedStreamChannel>> acceptListener, OptionMap optionMap) throws IOException {
				return defaultWorker.createStreamServer(bindAddress, acceptListener, optionMap);
			}

			@Deprecated
			@Override
			public IoFuture<ConnectedStreamChannel> connectStream(SocketAddress destination, ChannelListener<? super ConnectedStreamChannel> openListener, OptionMap optionMap) {
				return defaultWorker.connectStream(destination, openListener, optionMap);
			}

			@Deprecated
			@Override
			public IoFuture<ConnectedStreamChannel> connectStream(SocketAddress destination, ChannelListener<? super ConnectedStreamChannel> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.connectStream(destination, openListener, bindListener, optionMap);
			}

			@Deprecated
			@Override
			public IoFuture<ConnectedStreamChannel> connectStream(SocketAddress bindAddress, SocketAddress destination, ChannelListener<? super ConnectedStreamChannel> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.connectStream(bindAddress, destination, openListener, bindListener, optionMap);
			}

			@Override
			public IoFuture<StreamConnection> openStreamConnection(SocketAddress destination, ChannelListener<? super StreamConnection> openListener, OptionMap optionMap) {
				return defaultWorker.openStreamConnection(destination, openListener, optionMap);
			}

			@Override
			public IoFuture<StreamConnection> openStreamConnection(SocketAddress destination, ChannelListener<? super StreamConnection> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.openStreamConnection(destination, openListener, bindListener, optionMap);
			}

			@Override
			public IoFuture<StreamConnection> openStreamConnection(SocketAddress bindAddress, SocketAddress destination, ChannelListener<? super StreamConnection> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.openStreamConnection(bindAddress, destination, openListener, bindListener, optionMap);
			}

			@Deprecated
			@Override
			public IoFuture<ConnectedStreamChannel> acceptStream(SocketAddress destination, ChannelListener<? super ConnectedStreamChannel> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.acceptStream(destination, openListener, bindListener, optionMap);
			}

			@Override
			public IoFuture<StreamConnection> acceptStreamConnection(SocketAddress destination, ChannelListener<? super StreamConnection> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.acceptStreamConnection(destination, openListener, bindListener, optionMap);
			}

			@Deprecated
			@Override
			public IoFuture<ConnectedMessageChannel> connectDatagram(SocketAddress destination, ChannelListener<? super ConnectedMessageChannel> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.connectDatagram(destination, openListener, bindListener, optionMap);
			}

			@Deprecated
			@Override
			public IoFuture<ConnectedMessageChannel> connectDatagram(SocketAddress bindAddress, SocketAddress destination, ChannelListener<? super ConnectedMessageChannel> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.connectDatagram(bindAddress, destination, openListener, bindListener, optionMap);
			}

			@Override
			public IoFuture<MessageConnection> openMessageConnection(SocketAddress destination, ChannelListener<? super MessageConnection> openListener, OptionMap optionMap) {
				return defaultWorker.openMessageConnection(destination, openListener, optionMap);
			}

			@Deprecated
			@Override
			public IoFuture<ConnectedMessageChannel> acceptDatagram(SocketAddress destination, ChannelListener<? super ConnectedMessageChannel> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.acceptDatagram(destination, openListener, bindListener, optionMap);
			}

			@Override
			public IoFuture<MessageConnection> acceptMessageConnection(SocketAddress destination, ChannelListener<? super MessageConnection> openListener, ChannelListener<? super BoundChannel> bindListener, OptionMap optionMap) {
				return defaultWorker.acceptMessageConnection(destination, openListener, bindListener, optionMap);
			}

			@Override
			public MulticastMessageChannel createUdpServer(InetSocketAddress bindAddress, ChannelListener<? super MulticastMessageChannel> bindListener, OptionMap optionMap) throws IOException {
				return defaultWorker.createUdpServer(bindAddress, bindListener, optionMap);
			}

			@Override
			public MulticastMessageChannel createUdpServer(InetSocketAddress bindAddress, OptionMap optionMap) throws IOException {
				return defaultWorker.createUdpServer(bindAddress, optionMap);
			}

			@Deprecated
			@Override
			public void createPipe(ChannelListener<? super StreamChannel> leftOpenListener, ChannelListener<? super StreamChannel> rightOpenListener, OptionMap optionMap) throws IOException {
				defaultWorker.createPipe(leftOpenListener, rightOpenListener, optionMap);
			}

			@Deprecated
			@Override
			public void createOneWayPipe(ChannelListener<? super StreamSourceChannel> sourceListener, ChannelListener<? super StreamSinkChannel> sinkListener, OptionMap optionMap) throws IOException {
				defaultWorker.createOneWayPipe(sourceListener, sinkListener, optionMap);
			}

			@Override
			public StreamSourceChannel getInflatingChannel(StreamSourceChannel delegate, OptionMap options) throws IOException {
				return defaultWorker.getInflatingChannel(delegate, options);
			}


			@Override
			public StreamSinkChannel getDeflatingChannel(StreamSinkChannel delegate, OptionMap options) throws IOException {
				return defaultWorker.getDeflatingChannel(delegate, options);
			}


			@Override
			public ChannelPipe<StreamChannel, StreamChannel> createFullDuplexPipe() throws IOException {
				return defaultWorker.createFullDuplexPipe();
			}

			@Override
			public ChannelPipe<StreamConnection, StreamConnection> createFullDuplexPipeConnection() throws IOException {
				return defaultWorker.createFullDuplexPipeConnection();
			}

			@Override
			public ChannelPipe<StreamSourceChannel, StreamSinkChannel> createHalfDuplexPipe() throws IOException {
				return defaultWorker.createHalfDuplexPipe();
			}

			@Override
			public ChannelPipe<StreamConnection, StreamConnection> createFullDuplexPipeConnection(XnioIoFactory peer) throws IOException {
				return defaultWorker.createFullDuplexPipeConnection(peer);
			}

			@Override
			public ChannelPipe<StreamSourceChannel, StreamSinkChannel> createHalfDuplexPipe(XnioIoFactory peer) throws IOException {
				return defaultWorker.createHalfDuplexPipe(peer);
			}

			@Override
			public void execute(Runnable command) {
				defaultWorker.execute(command);
			}

			@Override
			public boolean supportsOption(Option<?> option) {
				return defaultWorker.supportsOption(option);
			}

			@Override
			public <T> T getOption(Option<T> option) throws IOException {
				return defaultWorker.getOption(option);
			}

			@Override
			public <T> T setOption(Option<T> option, T value) throws IllegalArgumentException, IOException {
				return defaultWorker.setOption(option, value);
			}

			@Override
			public Xnio getXnio() {
				return defaultWorker.getXnio();
			}

			@Override
			public String getName() {
				return defaultWorker.getName();
			}

			@Override
			public InetSocketAddress getBindAddress(InetAddress destination) {
				return defaultWorker.getBindAddress(destination);
			}


			@Override
			public Future<?> submit(Runnable task) {
				return defaultWorker.submit(task);
			}

			@Override
			public <T> Future<T> submit(Callable<T> task) {
				return defaultWorker.submit(task);
			}

			@Override
			public <T> Future<T> submit(Runnable task, T result) {
				return defaultWorker.submit(task, result);
			}

			@Override
			public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
				return defaultWorker.invokeAny(tasks);
			}

			@Override
			public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				return defaultWorker.invokeAny(tasks, timeout, unit);
			}

			@Override
			public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
				return defaultWorker.invokeAll(tasks, timeout, unit);
			}

			@Override
			public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
				return defaultWorker.invokeAll(tasks);
			}


			@Override
			public void run(Runnable runnable) {
				defaultWorker.run(runnable);
			}

			@Override
			public <R> R runAction(PrivilegedAction<R> action) {
				return defaultWorker.runAction(action);
			}

			@Override
			public <V> V runCallable(Callable<V> callable) throws Exception {
				return defaultWorker.runCallable(callable);
			}

			@Override
			public <R> R runExceptionAction(PrivilegedExceptionAction<R> action) throws PrivilegedActionException {
				return defaultWorker.runExceptionAction(action);
			}

			@Override
			public <T, U> void runBiConsumer(BiConsumer<T, U> consumer, T param1, U param2) {
				defaultWorker.runBiConsumer(consumer, param1, param2);
			}

			@Override
			public <T> void runConsumer(Consumer<T> consumer, T param) {
				defaultWorker.runConsumer(consumer, param);
			}

			@Override
			public <T, U, E extends Exception> void runExBiConsumer(ExceptionBiConsumer<T, U, E> consumer, T param1, U param2) throws E {
				defaultWorker.runExBiConsumer(consumer, param1, param2);
			}

			@Override
			public <T, E extends Exception> void runExConsumer(ExceptionConsumer<T, E> consumer, T param) throws E {
				defaultWorker.runExConsumer(consumer, param);
			}

			@Override
			public <T, U, R> R runBiFunction(BiFunction<T, U, R> function, T param1, U param2) {
				return defaultWorker.runBiFunction(function, param1, param2);
			}

			@Override
			public <T, U, R, E extends Exception> R runExBiFunction(ExceptionBiFunction<T, U, R, E> function, T param1, U param2) throws E {
				return defaultWorker.runExBiFunction(function, param1, param2);
			}

			@Override
			public <T, R> R runFunction(Function<T, R> function, T param) {
				return defaultWorker.runFunction(function, param);
			}

			@Override
			public <T, R, E extends Exception> R runExFunction(ExceptionFunction<T, R, E> function, T param) throws E {
				return defaultWorker.runExFunction(function, param);
			}

			@Override
			public <T, U> boolean runBiPredicate(BiPredicate<T, U> predicate, T param1, U param2) {
				return defaultWorker.runBiPredicate(predicate, param1, param2);
			}

			@Override
			public <T, U, E extends Exception> boolean runExBiPredicate(ExceptionBiPredicate<T, U, E> predicate, T param1, U param2) throws E {
				return defaultWorker.runExBiPredicate(predicate, param1, param2);
			}

			@Override
			public <T> boolean runPredicate(Predicate<T> predicate, T param) {
				return defaultWorker.runPredicate(predicate, param);
			}

			@Override
			public <T, E extends Exception> boolean runExPredicate(ExceptionPredicate<T, E> predicate, T param) throws E {
				return defaultWorker.runExPredicate(predicate, param);
			}

			@Override
			public <T> T runIntFunction(IntFunction<T> function, int value) {
				return defaultWorker.runIntFunction(function, value);
			}

			@Override
			public <T, E extends Exception> T runExIntFunction(ExceptionIntFunction<T, E> function, int value) throws E {
				return defaultWorker.runExIntFunction(function, value);
			}

			@Override
			public <T> T runLongFunction(LongFunction<T> function, long value) {
				return defaultWorker.runLongFunction(function, value);
			}

			@Override
			public <T, E extends Exception> T runExLongFunction(ExceptionLongFunction<T, E> function, long value) throws E {
				return defaultWorker.runExLongFunction(function, value);
			}
		};
	}
}
