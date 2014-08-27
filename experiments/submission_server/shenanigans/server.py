import tornado.ioloop
import tornado.web
import shenanigans_pb2
from itertools import izip

class MainHandler(tornado.web.RequestHandler):

    def get(self):
        #POST data only.
        self.write("")
    
    def validateGroup(self, group, token):
        print("Validating {0} against {1}\n".format(group.mac, token))
        return True
    
    def validate(self, submission):
        #Make sure that:
        #  *lengths of tokens and groups are the same and nonzero
        #  *each token validates
        if len(submission.group) != len(submission.token):
            print("Length mismatch in submission tokens.\n")
            return False
        if len(submission.group) < 1:
            print("No submissions in request.")
        for token, group in izip(submission.token, submission.group):
            if not self.validateGroup(group, token):
                return False
        
        return True


    def post(self):
        # Parse, validate, persist, and generate a certificate.
        submission = shenanigans_pb2.Submission();
        submission.ParseFromString(self.request.body);
        #print(submission)
        self.validate(submission)
        for probeGroup in submission.group:
            print(probeGroup.mac)



if __name__ == "__main__":
    application = tornado.web.Application([
        (r"/submitFingerprint", MainHandler),
    ])
    application.listen(8000)
    tornado.ioloop.IOLoop.instance().start()

