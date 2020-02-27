package ru.alphatest.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ForkJoinPool;

// По условию нагрузка может быть до 1000 запросов в час. Может что-то в условии напутано??? т.к. это очень мало.
// Но все равно добавил обертку в отложенный результат на всякий случай
@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get user risk profile
     *
     * @param id user info ID
     * @return risk profile
     */
    @GetMapping("/{id}")
    public DeferredResult<ResponseEntity<?>> riskProfile(@PathVariable String id) {
        DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();
        if (id.matches(UserService.ID_PATTERN)) {
            ForkJoinPool.commonPool().submit(() -> {
                try {
                    output.setResult(ResponseEntity.ok(userService.resolveRiskProfile(id).name()));
                } catch (Exception e) {
                    output.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
                }
            });
        } else {
            output.setErrorResult(ResponseEntity.badRequest().body("Wrong id. Must match ID????"));
        }
        return output;
    }
}
