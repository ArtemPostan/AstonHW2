package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UserDTO extends RepresentationModel<UserDTO> {

    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "Иван Иванов", required = true)
    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @Schema(description = "Электронная почта пользователя", example = "user@mail.ru")
    @Email(message = "Некорректный формат почты")
    private String email;

    @Schema(description = "Возраст пользователя", example = "30")
    @Min(value = 0, message = "Возраст должен быть от 0")
    private Integer age;
}