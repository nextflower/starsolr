create or replace
trigger eps_task_trigger after insert or update or delete on EPS_RES_RELATION for each row  declare FLAG int;
begin
	SELECT count(*) INTO FLAG FROM eps_solr_task WHERE RES_THEMELIBID=:old.SOURCE_RESLIB AND RES_THEMEID=:old.SOURCE_RESID AND SYS_RESLIB=:old.DEST_RESLIB AND SYS_RESID=:old.DEST_RESID;  
if inserting then     
	insert into eps_solr_task(RES_THEMELIBID,RES_THEMEID,SYS_RESLIB,SYS_RESID,SYS_CREATED,SYS_LASTMODIFIED,STATUS,SOLR_PK)values(:new.SOURCE_RESLIB,:new.SOURCE_RESID,:new.DEST_RESLIB,:new.DEST_RESID,sysdate,sysdate,0,:new.SOURCE_RESLIB||'-'||:new.SOURCE_RESID||'-'||:new.DEST_RESLIB||'-'||:new.DEST_RESID); 
elsif updating then      
	IF FLAG=0 THEN 		
    insert into eps_solr_task(RES_THEMELIBID,RES_THEMEID,SYS_RESLIB,SYS_RESID,SYS_CREATED,SYS_LASTMODIFIED,STATUS,SOLR_PK)values(:new.SOURCE_RESLIB,:new.SOURCE_RESID,:new.DEST_RESLIB,:new.DEST_RESID,sysdate,sysdate,0,:new.SOURCE_RESLIB||'-'||:new.SOURCE_RESID||'-'||:new.DEST_RESLIB||'-'||:new.DEST_RESID);     
	END IF; 
elsif deleting then 	
	IF FLAG=0 THEN 		
    insert into eps_solr_task(RES_THEMELIBID,RES_THEMEID,SYS_RESLIB,SYS_RESID,SYS_CREATED,SYS_LASTMODIFIED,STATUS,SOLR_PK)values(:old.SOURCE_RESLIB,:old.SOURCE_RESID,:old.DEST_RESLIB,:old.DEST_RESID,sysdate,sysdate,1,:new.SOURCE_RESLIB||'-'||:new.SOURCE_RESID||'-'||:new.DEST_RESLIB||'-'||:new.DEST_RESID); 	
	ELSE 		
    update eps_solr_task set STATUS = 1 where RES_THEMELIBID=:old.SOURCE_RESLIB AND RES_THEMEID=:old.SOURCE_RESID AND SYS_RESLIB=:old.DEST_RESLIB AND SYS_RESID=:old.DEST_RESID; 	
	END IF; 
end if; 
end;