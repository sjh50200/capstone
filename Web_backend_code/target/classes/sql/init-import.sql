INSERT INTO member (id, pw, car_num, seat_num, entry_time) VALUES ('TestUser1', '1234', '1234', 1, now());
INSERT INTO member (id, pw, car_num) VALUES ('TestUser2', '5678', '5678');
/*INSERT INTO accident (acc_time, car_num1, car_num2) VALUES ('2021-01-01 13:30:50', '12345', '67890');*/
INSERT INTO seat (seat_num, car_num, status) values (1, '1234', 'parkIn');
INSERT INTO seat (seat_num) values (2);
INSERT INTO seat (seat_num) values (3);
INSERT INTO seat (seat_num) values (4);