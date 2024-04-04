package avlyakulov.timur.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Getter
@Setter
@Value(staticConstructor = "of")
public class User {

    Integer id;

    String username;

    String password;
}