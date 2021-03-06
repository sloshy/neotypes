package neotypes
package refined

import exceptions.{PropertyNotFoundException, UncoercibleException}
import mappers.{ParameterMapper, ResultMapper, TypeHint, ValueMapper}
import types.QueryParam

import eu.timepit.refined.api.{Refined, RefinedType}
import org.neo4j.driver.v1.Value

object implicits {
  implicit def refinedValueMapper[T, P](implicit mapper: ValueMapper[T], rt: RefinedType.AuxT[Refined[T, P], T]): ValueMapper[Refined[T, P]] =
    new ValueMapper[Refined[T, P]] {
      override def to(fieldName: String, value: Option[Value]): Either[Throwable, Refined[T, P]] =
        value match {
          case None =>
            Left(PropertyNotFoundException(s"Property $fieldName not found"))

          case Some(value) =>
            mapper
              .to(fieldName, Some(value))
              .right
              .flatMap(t => rt.refine(t).left.map(msg => UncoercibleException(msg, None.orNull)))
        }
    }

  implicit def refinedResultMapper[T, P](implicit marshallable: ValueMapper[Refined[T, P]]): ResultMapper[Refined[T, P]] =
    new ResultMapper[Refined[T, P]] {
      override def to(fields: Seq[(String, Value)], typeHint: Option[TypeHint]): Either[Throwable, Refined[T, P]] =
        fields
          .headOption
          .fold(ifEmpty = marshallable.to("", None)) {
            case (name, value) => marshallable.to(name, Some(value))
          }
    }

  implicit def RefinedParameterMapper[T, P](implicit mapper: ParameterMapper[T]): ParameterMapper[Refined[T, P]] =
    mapper.contramap(refinedValue => refinedValue.value)
}
