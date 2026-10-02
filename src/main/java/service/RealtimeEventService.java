package service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealtimeEventService {
    private final CopyOnWriteArrayList<SseEmitter> clients = new CopyOnWriteArrayList<>();
    public SseEmitter subscribe(){
        SseEmitter emitter=new SseEmitter(0L);
        clients.add(emitter);
        emitter.onCompletion(()->clients.remove(emitter));
        emitter.onTimeout(()->{clients.remove(emitter); emitter.complete();});
        emitter.onError(e->clients.remove(emitter));
        try{emitter.send(SseEmitter.event().name("connected").data("CONTROL_VEHICULAR"));}catch(IOException e){clients.remove(emitter);}
        return emitter;
    }
    public void publish(String type,Object data){
        for(SseEmitter emitter:clients){
            try{emitter.send(SseEmitter.event().name(type).data(data));}
            catch(IOException e){clients.remove(emitter); emitter.complete();}
        }
    }
}
