# AlphaTest
В задаче есть условие что записи могут дублироваться а также могут отсутствовать часть данных, то таблица в БД создана самая простая без всяких ограничений
<pre>
CREATE TABLE users (
	id varchar(6),
	user_type varchar(6),
	black_list boolean
)
</pre>

Имея это пришлось отказаться от сущностей hibernate, а работать напрямую с запросами.
Эндпоинт для получения риск профиля на http://localhost:8080/api/users/{id}
Адрес к БД прописывается в application.properties. После этого создать таблицу указанным выше скриптом. 

Дайте знать если я неверно понял условия задачи)
