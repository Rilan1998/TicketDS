package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

class Seat {
    private int seatID=-1;
    private AtomicInteger src_dst=new AtomicInteger(0);
    public boolean onwrite=false;

    //初始化
    public Seat(int id,int departure,int arrival){
        this.seatID=id;
    }

    //检查有无空座，有空座返回true
    public int Check(int departure,int arrival){
        short tmp=Ticket2src_dst(departure,arrival);
        int counter=0;
        while (this.onwrite){
            counter++;
            if(counter==10000)
                return -1;
        }
        if(((this.src_dst.get()&tmp)==0)){
            return this.src_dst.get();
        }
        return -1;
    }

    //CAS买票
    public boolean CompareAndBuy(int expect,int value){
        int counter=0;
        while (this.onwrite){
            counter++;
            if(counter==10000)
                return false;
        }
        this.onwrite=true;
        if(this.src_dst.compareAndSet(expect,this.src_dst.get()|value)){
            this.onwrite=false;
            return true;
        }
        else
            this.onwrite=false;
        return false;
    }
    //退款
    public void Refund(int departure,int arrival){
        int i=0;
        for(int j=departure+1;j<=arrival;j++){
            i+=(1<<j);
        }
        this.src_dst.set(this.src_dst.get()^i);

    }
    //转换
    static public short Ticket2src_dst(int departure, int arrival) {
        short i=0;
        for(int j=departure+1;j<=arrival;j++){
            i+=(1<<j);
        }
        return i;
    }


}


class Route{
    private int routeID;
    private int coachnum;
    private int seatnum;
    private Seat seats[];

    //初始化
    public Route(int id,int coachnum,int seatnum){
        this.routeID=id;
        this.coachnum=coachnum;
        this.seatnum=seatnum;
        seats=new Seat[seatnum*coachnum];
        for(int i=0;i<seatnum*coachnum;i++){
            seats[i]=new Seat(i,0,0);
        }
    }

    //遍历返回空座位数量
    public int Inquiry(int departure,int arrival){
        int counter=0;
        for(int i=this.seatnum*this.coachnum-1;i>=0;i--){
            if(this.seats[i].Check(departure,arrival)!=-1)
                counter++;
        }
        return counter;
    }

    //查找空座位并买票
    public Ticket Inquiry_And_Buy(String passenger,int departure,int arrival,long tid){
        int i=0;
        int temp=Seat.Ticket2src_dst(departure,arrival);
        for(;i<this.seatnum*this.coachnum;i++){
            int j=this.seats[i].Check(departure,arrival);
            if(j!=-1){
                if(this.seats[i].CompareAndBuy(j, temp)) {
                    //currentSeatNum.decrementAndGet();
                    break;
                }
            }
        }
        if(i==this.seatnum*this.coachnum)
            return null;
        Ticket tmp=new Ticket();
        tmp.seat=i%this.seatnum+1;
        tmp.coach=i/this.seatnum+1;
        tmp.arrival=arrival;
        tmp.departure=departure;
        tmp.passenger=passenger;
        tmp.tid=tid;
        tmp.route=this.routeID;
        return tmp;
    }

    //指定座退票
    public void refund(Ticket ticket){
        this.seats[(ticket.coach-1)*(this.seatnum)+(ticket.seat-1)].Refund(ticket.departure,ticket.arrival);
    }
}


