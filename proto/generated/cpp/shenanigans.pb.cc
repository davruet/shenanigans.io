// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: shenanigans.proto

#define INTERNAL_SUPPRESS_PROTOBUF_FIELD_DEPRECATION
#include "shenanigans.pb.h"

#include <algorithm>

#include <google/protobuf/stubs/common.h>
#include <google/protobuf/stubs/once.h>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/wire_format_lite_inl.h>
// @@protoc_insertion_point(includes)

namespace io {
namespace shenanigans {
namespace proto {

void protobuf_ShutdownFile_shenanigans_2eproto() {
  delete Submission::default_instance_;
  delete Submission_ProbeGroup::default_instance_;
  delete Submission_ProbeGroup_ProbeReq::default_instance_;
}

#ifdef GOOGLE_PROTOBUF_NO_STATIC_INITIALIZER
void protobuf_AddDesc_shenanigans_2eproto_impl() {
  GOOGLE_PROTOBUF_VERIFY_VERSION;

#else
void protobuf_AddDesc_shenanigans_2eproto() {
  static bool already_here = false;
  if (already_here) return;
  already_here = true;
  GOOGLE_PROTOBUF_VERIFY_VERSION;

#endif
  Submission::default_instance_ = new Submission();
  Submission_ProbeGroup::default_instance_ = new Submission_ProbeGroup();
  Submission_ProbeGroup_ProbeReq::default_instance_ = new Submission_ProbeGroup_ProbeReq();
  Submission::default_instance_->InitAsDefaultInstance();
  Submission_ProbeGroup::default_instance_->InitAsDefaultInstance();
  Submission_ProbeGroup_ProbeReq::default_instance_->InitAsDefaultInstance();
  ::google::protobuf::internal::OnShutdown(&protobuf_ShutdownFile_shenanigans_2eproto);
}

#ifdef GOOGLE_PROTOBUF_NO_STATIC_INITIALIZER
GOOGLE_PROTOBUF_DECLARE_ONCE(protobuf_AddDesc_shenanigans_2eproto_once_);
void protobuf_AddDesc_shenanigans_2eproto() {
  ::google::protobuf::::google::protobuf::GoogleOnceInit(&protobuf_AddDesc_shenanigans_2eproto_once_,
                 &protobuf_AddDesc_shenanigans_2eproto_impl);
}
#else
// Force AddDescriptors() to be called at static initialization time.
struct StaticDescriptorInitializer_shenanigans_2eproto {
  StaticDescriptorInitializer_shenanigans_2eproto() {
    protobuf_AddDesc_shenanigans_2eproto();
  }
} static_descriptor_initializer_shenanigans_2eproto_;
#endif

// ===================================================================

#ifndef _MSC_VER
const int Submission_ProbeGroup_ProbeReq::kSsidFieldNumber;
const int Submission_ProbeGroup_ProbeReq::kReqBytesFieldNumber;
#endif  // !_MSC_VER

Submission_ProbeGroup_ProbeReq::Submission_ProbeGroup_ProbeReq()
  : ::google::protobuf::MessageLite() {
  SharedCtor();
}

void Submission_ProbeGroup_ProbeReq::InitAsDefaultInstance() {
}

Submission_ProbeGroup_ProbeReq::Submission_ProbeGroup_ProbeReq(const Submission_ProbeGroup_ProbeReq& from)
  : ::google::protobuf::MessageLite() {
  SharedCtor();
  MergeFrom(from);
}

void Submission_ProbeGroup_ProbeReq::SharedCtor() {
  _cached_size_ = 0;
  ssid_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  reqbytes_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

Submission_ProbeGroup_ProbeReq::~Submission_ProbeGroup_ProbeReq() {
  SharedDtor();
}

void Submission_ProbeGroup_ProbeReq::SharedDtor() {
  if (ssid_ != &::google::protobuf::internal::kEmptyString) {
    delete ssid_;
  }
  if (reqbytes_ != &::google::protobuf::internal::kEmptyString) {
    delete reqbytes_;
  }
  #ifdef GOOGLE_PROTOBUF_NO_STATIC_INITIALIZER
  if (this != &default_instance()) {
  #else
  if (this != default_instance_) {
  #endif
  }
}

void Submission_ProbeGroup_ProbeReq::SetCachedSize(int size) const {
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
}
const Submission_ProbeGroup_ProbeReq& Submission_ProbeGroup_ProbeReq::default_instance() {
#ifdef GOOGLE_PROTOBUF_NO_STATIC_INITIALIZER
  protobuf_AddDesc_shenanigans_2eproto();
#else
  if (default_instance_ == NULL) protobuf_AddDesc_shenanigans_2eproto();
#endif
  return *default_instance_;
}

Submission_ProbeGroup_ProbeReq* Submission_ProbeGroup_ProbeReq::default_instance_ = NULL;

Submission_ProbeGroup_ProbeReq* Submission_ProbeGroup_ProbeReq::New() const {
  return new Submission_ProbeGroup_ProbeReq;
}

void Submission_ProbeGroup_ProbeReq::Clear() {
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (has_ssid()) {
      if (ssid_ != &::google::protobuf::internal::kEmptyString) {
        ssid_->clear();
      }
    }
    if (has_reqbytes()) {
      if (reqbytes_ != &::google::protobuf::internal::kEmptyString) {
        reqbytes_->clear();
      }
    }
  }
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

bool Submission_ProbeGroup_ProbeReq::MergePartialFromCodedStream(
    ::google::protobuf::io::CodedInputStream* input) {
#define DO_(EXPRESSION) if (!(EXPRESSION)) return false
  ::google::protobuf::uint32 tag;
  while ((tag = input->ReadTag()) != 0) {
    switch (::google::protobuf::internal::WireFormatLite::GetTagFieldNumber(tag)) {
      // optional string ssid = 1;
      case 1: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_ssid()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(18)) goto parse_reqBytes;
        break;
      }

      // required bytes reqBytes = 2;
      case 2: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_reqBytes:
          DO_(::google::protobuf::internal::WireFormatLite::ReadBytes(
                input, this->mutable_reqbytes()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectAtEnd()) return true;
        break;
      }

      default: {
      handle_uninterpreted:
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_END_GROUP) {
          return true;
        }
        DO_(::google::protobuf::internal::WireFormatLite::SkipField(input, tag));
        break;
      }
    }
  }
  return true;
#undef DO_
}

void Submission_ProbeGroup_ProbeReq::SerializeWithCachedSizes(
    ::google::protobuf::io::CodedOutputStream* output) const {
  // optional string ssid = 1;
  if (has_ssid()) {
    ::google::protobuf::internal::WireFormatLite::WriteString(
      1, this->ssid(), output);
  }

  // required bytes reqBytes = 2;
  if (has_reqbytes()) {
    ::google::protobuf::internal::WireFormatLite::WriteBytes(
      2, this->reqbytes(), output);
  }

}

int Submission_ProbeGroup_ProbeReq::ByteSize() const {
  int total_size = 0;

  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    // optional string ssid = 1;
    if (has_ssid()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->ssid());
    }

    // required bytes reqBytes = 2;
    if (has_reqbytes()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::BytesSize(
          this->reqbytes());
    }

  }
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = total_size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
  return total_size;
}

void Submission_ProbeGroup_ProbeReq::CheckTypeAndMergeFrom(
    const ::google::protobuf::MessageLite& from) {
  MergeFrom(*::google::protobuf::down_cast<const Submission_ProbeGroup_ProbeReq*>(&from));
}

void Submission_ProbeGroup_ProbeReq::MergeFrom(const Submission_ProbeGroup_ProbeReq& from) {
  GOOGLE_CHECK_NE(&from, this);
  if (from._has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (from.has_ssid()) {
      set_ssid(from.ssid());
    }
    if (from.has_reqbytes()) {
      set_reqbytes(from.reqbytes());
    }
  }
}

void Submission_ProbeGroup_ProbeReq::CopyFrom(const Submission_ProbeGroup_ProbeReq& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool Submission_ProbeGroup_ProbeReq::IsInitialized() const {
  if ((_has_bits_[0] & 0x00000002) != 0x00000002) return false;

  return true;
}

void Submission_ProbeGroup_ProbeReq::Swap(Submission_ProbeGroup_ProbeReq* other) {
  if (other != this) {
    std::swap(ssid_, other->ssid_);
    std::swap(reqbytes_, other->reqbytes_);
    std::swap(_has_bits_[0], other->_has_bits_[0]);
    std::swap(_cached_size_, other->_cached_size_);
  }
}

::std::string Submission_ProbeGroup_ProbeReq::GetTypeName() const {
  return "io.shenanigans.proto.Submission.ProbeGroup.ProbeReq";
}


// -------------------------------------------------------------------

#ifndef _MSC_VER
const int Submission_ProbeGroup::kMacFieldNumber;
const int Submission_ProbeGroup::kTokenFieldNumber;
const int Submission_ProbeGroup::kReqFieldNumber;
#endif  // !_MSC_VER

Submission_ProbeGroup::Submission_ProbeGroup()
  : ::google::protobuf::MessageLite() {
  SharedCtor();
}

void Submission_ProbeGroup::InitAsDefaultInstance() {
}

Submission_ProbeGroup::Submission_ProbeGroup(const Submission_ProbeGroup& from)
  : ::google::protobuf::MessageLite() {
  SharedCtor();
  MergeFrom(from);
}

void Submission_ProbeGroup::SharedCtor() {
  _cached_size_ = 0;
  mac_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  token_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

Submission_ProbeGroup::~Submission_ProbeGroup() {
  SharedDtor();
}

void Submission_ProbeGroup::SharedDtor() {
  if (mac_ != &::google::protobuf::internal::kEmptyString) {
    delete mac_;
  }
  if (token_ != &::google::protobuf::internal::kEmptyString) {
    delete token_;
  }
  #ifdef GOOGLE_PROTOBUF_NO_STATIC_INITIALIZER
  if (this != &default_instance()) {
  #else
  if (this != default_instance_) {
  #endif
  }
}

void Submission_ProbeGroup::SetCachedSize(int size) const {
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
}
const Submission_ProbeGroup& Submission_ProbeGroup::default_instance() {
#ifdef GOOGLE_PROTOBUF_NO_STATIC_INITIALIZER
  protobuf_AddDesc_shenanigans_2eproto();
#else
  if (default_instance_ == NULL) protobuf_AddDesc_shenanigans_2eproto();
#endif
  return *default_instance_;
}

Submission_ProbeGroup* Submission_ProbeGroup::default_instance_ = NULL;

Submission_ProbeGroup* Submission_ProbeGroup::New() const {
  return new Submission_ProbeGroup;
}

void Submission_ProbeGroup::Clear() {
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (has_mac()) {
      if (mac_ != &::google::protobuf::internal::kEmptyString) {
        mac_->clear();
      }
    }
    if (has_token()) {
      if (token_ != &::google::protobuf::internal::kEmptyString) {
        token_->clear();
      }
    }
  }
  req_.Clear();
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

bool Submission_ProbeGroup::MergePartialFromCodedStream(
    ::google::protobuf::io::CodedInputStream* input) {
#define DO_(EXPRESSION) if (!(EXPRESSION)) return false
  ::google::protobuf::uint32 tag;
  while ((tag = input->ReadTag()) != 0) {
    switch (::google::protobuf::internal::WireFormatLite::GetTagFieldNumber(tag)) {
      // optional string mac = 1;
      case 1: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_mac()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(18)) goto parse_token;
        break;
      }

      // optional string token = 2;
      case 2: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_token:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_token()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(26)) goto parse_req;
        break;
      }

      // repeated .io.shenanigans.proto.Submission.ProbeGroup.ProbeReq req = 3;
      case 3: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_req:
          DO_(::google::protobuf::internal::WireFormatLite::ReadMessageNoVirtual(
                input, add_req()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(26)) goto parse_req;
        if (input->ExpectAtEnd()) return true;
        break;
      }

      default: {
      handle_uninterpreted:
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_END_GROUP) {
          return true;
        }
        DO_(::google::protobuf::internal::WireFormatLite::SkipField(input, tag));
        break;
      }
    }
  }
  return true;
#undef DO_
}

void Submission_ProbeGroup::SerializeWithCachedSizes(
    ::google::protobuf::io::CodedOutputStream* output) const {
  // optional string mac = 1;
  if (has_mac()) {
    ::google::protobuf::internal::WireFormatLite::WriteString(
      1, this->mac(), output);
  }

  // optional string token = 2;
  if (has_token()) {
    ::google::protobuf::internal::WireFormatLite::WriteString(
      2, this->token(), output);
  }

  // repeated .io.shenanigans.proto.Submission.ProbeGroup.ProbeReq req = 3;
  for (int i = 0; i < this->req_size(); i++) {
    ::google::protobuf::internal::WireFormatLite::WriteMessage(
      3, this->req(i), output);
  }

}

int Submission_ProbeGroup::ByteSize() const {
  int total_size = 0;

  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    // optional string mac = 1;
    if (has_mac()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->mac());
    }

    // optional string token = 2;
    if (has_token()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->token());
    }

  }
  // repeated .io.shenanigans.proto.Submission.ProbeGroup.ProbeReq req = 3;
  total_size += 1 * this->req_size();
  for (int i = 0; i < this->req_size(); i++) {
    total_size +=
      ::google::protobuf::internal::WireFormatLite::MessageSizeNoVirtual(
        this->req(i));
  }

  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = total_size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
  return total_size;
}

void Submission_ProbeGroup::CheckTypeAndMergeFrom(
    const ::google::protobuf::MessageLite& from) {
  MergeFrom(*::google::protobuf::down_cast<const Submission_ProbeGroup*>(&from));
}

void Submission_ProbeGroup::MergeFrom(const Submission_ProbeGroup& from) {
  GOOGLE_CHECK_NE(&from, this);
  req_.MergeFrom(from.req_);
  if (from._has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (from.has_mac()) {
      set_mac(from.mac());
    }
    if (from.has_token()) {
      set_token(from.token());
    }
  }
}

void Submission_ProbeGroup::CopyFrom(const Submission_ProbeGroup& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool Submission_ProbeGroup::IsInitialized() const {

  for (int i = 0; i < req_size(); i++) {
    if (!this->req(i).IsInitialized()) return false;
  }
  return true;
}

void Submission_ProbeGroup::Swap(Submission_ProbeGroup* other) {
  if (other != this) {
    std::swap(mac_, other->mac_);
    std::swap(token_, other->token_);
    req_.Swap(&other->req_);
    std::swap(_has_bits_[0], other->_has_bits_[0]);
    std::swap(_cached_size_, other->_cached_size_);
  }
}

::std::string Submission_ProbeGroup::GetTypeName() const {
  return "io.shenanigans.proto.Submission.ProbeGroup";
}


// -------------------------------------------------------------------

#ifndef _MSC_VER
const int Submission::kGroupFieldNumber;
#endif  // !_MSC_VER

Submission::Submission()
  : ::google::protobuf::MessageLite() {
  SharedCtor();
}

void Submission::InitAsDefaultInstance() {
}

Submission::Submission(const Submission& from)
  : ::google::protobuf::MessageLite() {
  SharedCtor();
  MergeFrom(from);
}

void Submission::SharedCtor() {
  _cached_size_ = 0;
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

Submission::~Submission() {
  SharedDtor();
}

void Submission::SharedDtor() {
  #ifdef GOOGLE_PROTOBUF_NO_STATIC_INITIALIZER
  if (this != &default_instance()) {
  #else
  if (this != default_instance_) {
  #endif
  }
}

void Submission::SetCachedSize(int size) const {
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
}
const Submission& Submission::default_instance() {
#ifdef GOOGLE_PROTOBUF_NO_STATIC_INITIALIZER
  protobuf_AddDesc_shenanigans_2eproto();
#else
  if (default_instance_ == NULL) protobuf_AddDesc_shenanigans_2eproto();
#endif
  return *default_instance_;
}

Submission* Submission::default_instance_ = NULL;

Submission* Submission::New() const {
  return new Submission;
}

void Submission::Clear() {
  group_.Clear();
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

bool Submission::MergePartialFromCodedStream(
    ::google::protobuf::io::CodedInputStream* input) {
#define DO_(EXPRESSION) if (!(EXPRESSION)) return false
  ::google::protobuf::uint32 tag;
  while ((tag = input->ReadTag()) != 0) {
    switch (::google::protobuf::internal::WireFormatLite::GetTagFieldNumber(tag)) {
      // repeated .io.shenanigans.proto.Submission.ProbeGroup group = 1;
      case 1: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_group:
          DO_(::google::protobuf::internal::WireFormatLite::ReadMessageNoVirtual(
                input, add_group()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(10)) goto parse_group;
        if (input->ExpectAtEnd()) return true;
        break;
      }

      default: {
      handle_uninterpreted:
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_END_GROUP) {
          return true;
        }
        DO_(::google::protobuf::internal::WireFormatLite::SkipField(input, tag));
        break;
      }
    }
  }
  return true;
#undef DO_
}

void Submission::SerializeWithCachedSizes(
    ::google::protobuf::io::CodedOutputStream* output) const {
  // repeated .io.shenanigans.proto.Submission.ProbeGroup group = 1;
  for (int i = 0; i < this->group_size(); i++) {
    ::google::protobuf::internal::WireFormatLite::WriteMessage(
      1, this->group(i), output);
  }

}

int Submission::ByteSize() const {
  int total_size = 0;

  // repeated .io.shenanigans.proto.Submission.ProbeGroup group = 1;
  total_size += 1 * this->group_size();
  for (int i = 0; i < this->group_size(); i++) {
    total_size +=
      ::google::protobuf::internal::WireFormatLite::MessageSizeNoVirtual(
        this->group(i));
  }

  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = total_size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
  return total_size;
}

void Submission::CheckTypeAndMergeFrom(
    const ::google::protobuf::MessageLite& from) {
  MergeFrom(*::google::protobuf::down_cast<const Submission*>(&from));
}

void Submission::MergeFrom(const Submission& from) {
  GOOGLE_CHECK_NE(&from, this);
  group_.MergeFrom(from.group_);
}

void Submission::CopyFrom(const Submission& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool Submission::IsInitialized() const {

  for (int i = 0; i < group_size(); i++) {
    if (!this->group(i).IsInitialized()) return false;
  }
  return true;
}

void Submission::Swap(Submission* other) {
  if (other != this) {
    group_.Swap(&other->group_);
    std::swap(_has_bits_[0], other->_has_bits_[0]);
    std::swap(_cached_size_, other->_cached_size_);
  }
}

::std::string Submission::GetTypeName() const {
  return "io.shenanigans.proto.Submission";
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace proto
}  // namespace shenanigans
}  // namespace io

// @@protoc_insertion_point(global_scope)
