create trigger JC_CONTENT_INSERT_TRIGGER after insert on JC_CONTENT for each row 
begin
INSERT INTO ${taskTableName} (CONTENT_ID, STATUS) VALUES(new.CONTENT_ID,0); 
end;

drop trigger if exists JC_CONTENT_UPDATE_TRIGGER;
create trigger JC_CONTENT_UPDATE_TRIGGER after update on JC_CONTENT for each row 
begin
INSERT INTO ${taskTableName} (CONTENT_ID, STATUS) VALUES(new.CONTENT_ID,0); 
end;

drop trigger if exists JC_CONTENT_DELETE_TRIGGER;
create trigger JC_CONTENT_DELETE_TRIGGER after delete on JC_CONTENT for each row 
begin
DELETE FROM ${taskTableName} WHERE CONTENT_ID = old.CONTENT_ID;
INSERT INTO ${taskTableName} (CONTENT_ID, STATUS) VALUES(new.CONTENT_ID,1); 
end;
