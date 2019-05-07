Norint paleisti web servisą reikia paleisti komandą:

docker-compose up --build -d

Tada galima eiti:

/todos/id - su GET pamatyti visus įrašus ir Link į Userius. su Patch/Put redaguoti įrašą. su DELETE ištrina įrašą.

/todos/id/users - su GET pamatyti visus įrašo Userius. su POST pridėti naują Userį

/todos?embed=users - su GET pamatyti visus įrašus ir Link į Userius

/todos/id/users/email - su GET gauti Userį. su Patch/Put redaguoti Userį. su DELETE ištrina Userį.