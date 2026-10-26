CREATE TABLE students(
    roll_number VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(20) NOT NULL,
    current_semester INT NOT NULL
);
CREATE TABLE courses (
    course_code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    credits INT NOT NULL CHECK (credits > 0),
    course_type VARCHAR(20) NOT NULL
        CHECK (course_type IN ('THEORY', 'PRACTICAL', 'EMBEDDED'))
);

CREATE TABLE enrollments (
    enrollment_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    roll_number VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    classes_held INT NOT NULL DEFAULT 0,
    classes_attended INT NOT NULL DEFAULT 0,
    medical_condonation_approved BOOLEAN NOT NULL DEFAULT FALSE,

    FOREIGN KEY (roll_number)
        REFERENCES students(roll_number),

    FOREIGN KEY (course_code)
        REFERENCES courses(course_code),

    UNIQUE (roll_number, course_code, semester),

    CHECK (semester BETWEEN 1 AND 8),
    CHECK (classes_held >= 0),
    CHECK (classes_attended >= 0),
    CHECK (classes_attended <= classes_held)
);

CREATE TABLE assessment_marks (
    enrollment_id INT PRIMARY KEY,
    cia1 NUMERIC(5,2),
    cia2 NUMERIC(5,2),
    assignments NUMERIC(5,2),
    quizzes NUMERIC(5,2),
    lab_evaluations NUMERIC(5,2),
    ese_marks NUMERIC(5,2),
    ese_absent BOOLEAN NOT NULL DEFAULT FALSE,

    FOREIGN KEY (enrollment_id)
        REFERENCES enrollments(enrollment_id),

    CHECK (cia1 IS NULL OR cia1 BETWEEN 0 AND 100),
    CHECK (cia2 IS NULL OR cia2 BETWEEN 0 AND 100),
    CHECK (assignments IS NULL OR assignments BETWEEN 0 AND 100),
    CHECK (quizzes IS NULL OR quizzes BETWEEN 0 AND 100),
    CHECK (lab_evaluations IS NULL OR lab_evaluations BETWEEN 0 AND 100),
    CHECK (ese_marks IS NULL OR ese_marks BETWEEN 0 AND 100),

    CHECK (ese_absent = FALSE OR ese_marks IS NULL)
);
