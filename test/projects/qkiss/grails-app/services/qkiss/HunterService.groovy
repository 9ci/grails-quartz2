package qkiss
import java.util.concurrent.atomic.AtomicInteger

class HunterService{

    static transactional = true

	static boughtTheTicket = false
	static tookTheRide = false
	static AtomicInteger rideCount = new AtomicInteger(0)
	static boughtTheTicketAndTookTheRide = false
	
	def quartzScheduler
	

	
	static void Buy_The_Ticket() {
		boughtTheTicket = true
    } 

    def takeTheRide(boolean go) {
		tookTheRide = go
		rideCount.getAndIncrement() 
		if(boughtTheTicket && tookTheRide) boughtTheTicketAndTookTheRide = true
    }

}
