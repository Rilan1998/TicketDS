package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
		
	//ToDo
	private int routenum = 5;
	private int coachnum = 8;
	private int seatnum = 100;
	private int stationnum = 10;
	private int threadnum = 16;
	private Route routes[];
	private AtomicLong tid= new AtomicLong();

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;
		this.routes=new Route[routenum+1];
		for(int i=1;i<=routenum;i++){
			this.routes[i]=new Route(i,coachnum,seatnum);
		}
	}
	public Ticket buyTicket(String passenger, int route, int departure, int arrival){
		return this.routes[route].Inquiry_And_Buy(passenger,departure,arrival,tid.getAndIncrement());
	}
	public int inquiry(int route, int departure, int arrival){
		return this.routes[route].Inquiry(departure,arrival);
	}
	public boolean refundTicket(Ticket ticket){
		this.routes[ticket.route].refund(ticket);
		return true;
	}
}
