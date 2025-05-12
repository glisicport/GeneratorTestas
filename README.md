# Test Generator i Question Manager

Ovaj projekat sadrži dva Java GUI programa razvijena uz pomoć **Maven** alata:

1. **TestGenerator** - GUI aplikacija za generisanje testova.
2. **QuestionManager** - GUI aplikacija za upravljanje pitanjima.

## Izgradnja i Pokretanje Aplikacija

Da biste izgradili i pokrenuli aplikacije, pratite sledeće komande:

```bash
# 1. Izgradnja TestGenerator aplikacije
mvn clean package -Ptest-generator

# Pokretanje TestGenerator aplikacije
java -jar target/test-generator.jar

# 2. Izgradnja QuestionManager aplikacije
mvn clean package -Pquestion-manager

# Pokretanje QuestionManager aplikacije
java -jar target/question-manager.jar
