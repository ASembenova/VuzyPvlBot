package kz.saa.vuzy_pvl_bot.telegram;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Service
public class BotStateCash {
    private final Map<Long, BotState> botStateMap = new HashMap<>();
    private final Map<Long, Stack<BotState>> botStateMap2 = new HashMap<>();

    public void saveBotState(Long userId, BotState botState){
        if(botStateMap2.containsKey(userId)){
            botStateMap2.get(userId).push(botState);
        }
        else {
            Stack<BotState> stack = new Stack<>();
            stack.push(botState);
            botStateMap2.put(userId, stack);
        }
    }

    public BotState getBotState(Long userId){
        if(!botStateMap2.containsKey(userId)){
            return BotState.START;
        }
        return botStateMap2.get(userId).peek();
    }

    public BotState popBotState(Long userId){
        return botStateMap2.get(userId).pop();
    }


    public Stack getStack(Long userId){
        return botStateMap2.get(userId);
    }


    public boolean isEmpty(){
        return botStateMap.isEmpty();
    }
}
