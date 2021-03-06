/*
 * Copyright (C) 2018 Marcus Hirt
 *                    www.hirt.se
 *
 * This software is free:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESSED OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (C) Marcus Hirt, 2018
 */
package se.hirt.examples.svc.jfr.control;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import se.hirt.examples.svc.jfr.fib.Fibonacci;
import se.hirt.examples.svc.jfr.fib.FibonacciEvent;

/**
 * Example program both controlling the recorder and parsing the resulting data.
 * <p>
 * The program will calculate the 50 first Fibonacci numbers, then printing them
 * out using the recorded data.
 * <p>
 * This will only work with JDK 9 and later. For a JDK 7+ compatible way of
 * parsing recordings, see the JMC examples.
 */
public class RecordAndConsume {
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.out.println("Need to specify a location for the recording!");
			System.exit(2);
		}

		Path path = Paths.get(args[0]);

		try (Recording recording = new Recording()) {
			recording.setName("Fibonacci Recording");
			recording.start();
			recording.enable(FibonacciEvent.class);
			for (int n = 0; n < 50; n++) {
				System.out.println("Calculating fib " + n);
				FibonacciEvent event = new FibonacciEvent();
				event.number = n;
				event.begin();
				event.algorithmName = FibonacciEvent.ALGORITHM_NAME_ITERATIVE;
				event.value = Fibonacci.fibonacciIterative(n);
				event.commit();
			}
			recording.stop();
			recording.dump(path);
			for (RecordedEvent event : RecordingFile.readAllEvents(path)) {
				int number = event.getValue("number");
				long value = event.getValue("value");
				System.out.printf("fibonacci(%d) = %d (time: %dns)\n", number, value, event.getDuration().getNano());
			}
		}
	}
}
