package cn.cuptec.faros.im.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class NewRecipeEvent extends ApplicationEvent {

    private String uid;

    private String doctorId;

    private Integer recipeId;

    public NewRecipeEvent(Object source) {
        super(source);
    }

}


