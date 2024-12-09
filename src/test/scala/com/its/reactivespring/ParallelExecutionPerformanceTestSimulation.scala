package com.its.reactivespring

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class ParallelExecutionPerformanceTestSimulation extends Simulation {

  // Simulated User List
  val users = List("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

  // Counters for Success and Failure
  val reactorSuccessCount = new AtomicInteger(0)
  val reactorFailureCount = new AtomicInteger(0)
  val jdkSuccessCount = new AtomicInteger(0)
  val jdkFailureCount = new AtomicInteger(0)

  // Wrapper for Reactor-based Implementation
  def reactorProcessUser(user: String): Unit = {
    Try {
      // Call the static method directly (update package name as needed)
      com.its.reactivespring.mc.ethbatch.ReactiveApplicationWithIsolatedExceptionForEachParallelThread.withFlatMap()
    } match {
      case Success(_) => reactorSuccessCount.incrementAndGet()
      case Failure(e) =>
        reactorFailureCount.incrementAndGet()
        println(s"Reactor processing failed for user: $user, Error: ${e.getMessage}")
    }
  }

  // Wrapper for JDK-based Implementation
  def jdkProcessUser(user: String): Unit = {
    Try {
      // Call the static method directly (update package name as needed)
      com.its.reactivespring.mc.ethbatch.Jdk17ApplicationWithIsolatedExceptionForEachParallelThread.withFlatMapUsingJDK()
    } match {
      case Success(_) => jdkSuccessCount.incrementAndGet()
      case Failure(e) =>
        jdkFailureCount.incrementAndGet()
        println(s"JDK processing failed for user: $user, Error: ${e.getMessage}")
    }
  }

  // Reactor Scenario
  val reactorScenario = scenario("Reactor Performance Test")
    .repeat(users.size) { // Simulate processing each user
      exec { session =>
        val user = users(session.userId.toInt % users.size)
        reactorProcessUser(user)
        session
      }
    }

  // JDK Scenario
  val jdkScenario = scenario("JDK Performance Test")
    .repeat(users.size) { // Simulate processing each user
      exec { session =>
        val user = users(session.userId.toInt % users.size)
        jdkProcessUser(user)
        session
      }
    }

  // Simulation Setup
  setUp(
    reactorScenario.inject(
      atOnceUsers(10), // Start 10 users simultaneously
      rampUsers(50).during(10.seconds) // Ramp up to 50 users over 10 seconds
    ),
    jdkScenario.inject(
      atOnceUsers(10),
      rampUsers(50).during(10.seconds)
    )
  ).maxDuration(1.minute)
    .assertions(
      global.successfulRequests.percent.gt(95)
    )

  // Print final success and failure counts
  after {
    println(s"Reactor Success Count: ${reactorSuccessCount.get()}, Reactor Failure Count: ${reactorFailureCount.get()}")
    println(s"JDK Success Count: ${jdkSuccessCount.get()}, JDK Failure Count: ${jdkFailureCount.get()}")
  }
}
